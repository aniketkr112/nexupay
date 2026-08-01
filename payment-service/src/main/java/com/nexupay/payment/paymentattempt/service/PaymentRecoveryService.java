package com.nexupay.payment.paymentattempt.service;

import com.nexupay.payment.bank.dto.BankResponse;
import com.nexupay.payment.bank.service.BankService;
import com.nexupay.payment.common.enums.PaymentAttemptStatus;
import com.nexupay.payment.paymentattempt.entity.PaymentAttempt;
import com.nexupay.payment.paymentattempt.repository.PaymentAttemptRepository;
import com.nexupay.payment.paymentattempt.service.transaction.PaymentAttemptTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecoveryService {

    private static final int RECOVERY_BATCH_SIZE = 100;
    private static final Duration RECOVERY_DELAY = Duration.ofMinutes(2);

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final BankService bankService;
    private final PaymentAttemptTransactionService paymentAttemptTransactionService;



    public void recoverPayments() {

        log.info("Payment recovery job started.");

        Pageable pageable = PageRequest.of(
                0,
                RECOVERY_BATCH_SIZE,
                Sort.by("createdAt").ascending()
        );

        LocalDateTime cutoffTime =
                LocalDateTime.now().minus(RECOVERY_DELAY);

        List<PaymentAttempt> recoverablePaymentAttempts =
                paymentAttemptRepository
                        .findByStatusAndCreatedAtBefore(
                                PaymentAttemptStatus.CREATED,
                                cutoffTime,
                                pageable
                        );

        log.info(
                "Found {} recoverable payment attempts.",
                recoverablePaymentAttempts.size()
        );

        if (recoverablePaymentAttempts.isEmpty()) {
            log.info("No recoverable payment attempts found.");
            return;
        }

        for (PaymentAttempt paymentAttempt : recoverablePaymentAttempts) {
            try {
                recoverSinglePaymentAttempt(paymentAttempt);
            } catch (Exception ex) {
                log.error(
                        "Recovery failed for attempt {}",
                        paymentAttempt.getAttemptId(),
                        ex
                );
            }
        }

    }
    private void recoverSinglePaymentAttempt(
            PaymentAttempt paymentAttempt) {

        BankResponse response =
                bankService.checkPaymentStatus(
                        paymentAttempt.getAttemptId()
                );

        paymentAttemptTransactionService
                .finalizePaymentAttempt(
                        paymentAttempt.getAttemptId(),
                        response
                );
        log.info(
                "Recovery completed for payment attempt {} with bank status {}.",
                paymentAttempt.getAttemptId(),
                response.getStatus()
        );
    }
}
