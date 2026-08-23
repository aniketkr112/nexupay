package com.nexupay.payment.refund.service;

import com.nexupay.payment.refund.entity.Refund;
import com.nexupay.payment.refund.enums.RefundStatus;
import com.nexupay.payment.refund.repository.RefundRepository;
import com.nexupay.payment.refund.service.transaction.RefundProcessingTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundProcessorImpl implements RefundProcessor {

    private final RefundRepository refundRepository;
    private final RefundProcessingTransactionService refundProcessingTransactionService;

    @Override
    public void processPendingRefunds() {

        log.info("Refund recover job started.");
        List<Refund> pendingRefunds =
                refundRepository.findByStatus(RefundStatus.PENDING);

        for (Refund refund : pendingRefunds) {
            try {
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
