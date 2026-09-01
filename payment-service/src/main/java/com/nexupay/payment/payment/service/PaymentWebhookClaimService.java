package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.enums.PaymentWebhookStatus;
import com.nexupay.payment.payment.repository.PaymentWebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentWebhookClaimService {

    private final PaymentWebhookRepository paymentWebhookRepository;

    @Transactional
    public boolean claimWebhook(Long webhookId, LocalDateTime expiryTime) {

        int updated = paymentWebhookRepository.claimWebhook(
                webhookId,
                PaymentWebhookStatus.PENDING,
                expiryTime
        );

        return updated == 1;
    }
}