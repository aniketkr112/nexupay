package com.nexupay.payment.payment.scheduler;

import com.nexupay.payment.common.enums.PaymentWebhookStatus;
import com.nexupay.payment.payment.entity.PaymentWebhook;
import com.nexupay.payment.payment.repository.PaymentWebhookRepository;
import com.nexupay.payment.payment.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookScheduler {

    private final PaymentWebhookRepository paymentWebhookRepository;
    private final PaymentWebhookService paymentWebhookService;

    @Scheduled(fixedDelay = 60000)
    public void processPendingWebhooks() {

        log.info("Starting payment webhook delivery job.");

        List<PaymentWebhook> webhooks =
                paymentWebhookRepository
                        .findByStatus(
                                PaymentWebhookStatus.PENDING,
                                PageRequest.of(0, 100)
                        );

        log.info(
                "Found {} pending payment webhooks.",
                webhooks.size()
        );

        for (PaymentWebhook webhook : webhooks) {
            try {
                paymentWebhookService.sendWebhook(webhook);
            } catch (Exception e) {
                log.error(
                        "Unexpected error while processing payment webhook {}.",
                        webhook.getId(),
                        e
                );
            }
        }
    }
}