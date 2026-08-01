package com.nexupay.payment.paymentattempt.scheduler;

import com.nexupay.payment.paymentattempt.service.PaymentRecoveryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentRecoveryScheduler {

    private final PaymentRecoveryService paymentRecoveryService;

    public PaymentRecoveryScheduler(PaymentRecoveryService paymentRecoveryService) {
        this.paymentRecoveryService = paymentRecoveryService;
    }

    @Scheduled(fixedDelay = 60000)
    public void recoverPayments() {
        paymentRecoveryService.recoverPayments();
    }
}