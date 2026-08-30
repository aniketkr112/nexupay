package com.nexupay.payment.refund.service;

import com.nexupay.payment.refund.entity.Refund;
import com.nexupay.payment.refund.enums.RefundStatus;
import com.nexupay.payment.refund.repository.RefundRepository;
import com.nexupay.payment.refund.service.transaction.RefundClaimService;
import com.nexupay.payment.refund.service.transaction.RefundProcessingTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundProcessorImpl implements RefundProcessor {

    private final RefundRepository refundRepository;
    private final RefundProcessingTransactionService refundProcessingTransactionService;
    private final RefundClaimService refundClaimService;

    @Override
    public void processPendingRefunds() {

        log.info("Refund processing job started.");

        Pageable limit = PageRequest.of(
                0,
                100,
                Sort.by("createdAt").ascending()
        );

        LocalDateTime expirationTime =
                LocalDateTime.now().minusMinutes(5);

        List<Refund> pendingRefunds =
                refundRepository.findUnclaimedRefunds(
                        RefundStatus.PENDING,
                        expirationTime,
                        limit
                );

        log.info(
                "Found {} pending refund request.",
                pendingRefunds.size()
        );

        if (pendingRefunds.isEmpty()) {
            log.info("No pending refund request found.");
            return;
        }

        for (Refund refund : pendingRefunds) {
            try {
                boolean claimed =
                        refundClaimService.claimRefund(refund.getId());

                if (!claimed) {
                    log.info(
                            "Refund {} was already claimed by another instance. Skipping.",
                            refund.getRefundId()
                    );
                    continue;
                }

                refundProcessingTransactionService.processSingleRefund(refund);
            } catch (Exception ex) {
                log.error(
                        "Failed to process refund {}",
                        refund.getRefundId(),
                        ex
                );
            }
        }
    }


}
