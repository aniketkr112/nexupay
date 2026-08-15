package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.exception.PaymentNotFoundException;
import com.nexupay.payment.payment.dto.webhook.PaymentWebhookPayload;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.entity.PaymentWebhook;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.payment.repository.PaymentWebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
public class PaymentWebhookService {
    private static final int MAX_WEBHOOK_ATTEMPTS = 3;

    private final RestClient restClient;
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookRepository paymentWebhookRepository;


    public void sendWebhook(PaymentWebhook paymentWebhook) {

        Payment payment = paymentRepository
                .findByPaymentId(paymentWebhook.getPaymentId())
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                paymentWebhook.getPaymentId()
                        )
                );

        PaymentWebhookPayload payload =
                PaymentWebhookPayload.from(payment);

        try {
            restClient.post()
                    .uri(paymentWebhook.getWebhookUrl())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            paymentWebhook.markSuccess();

        } catch (RestClientResponseException e) {
            handleDeliveryFailure(paymentWebhook);
        } catch (RestClientException e) {
            handleDeliveryFailure(paymentWebhook);
        }
        paymentWebhookRepository.save(paymentWebhook);
    }

    private void handleDeliveryFailure(PaymentWebhook paymentWebhook) {

        paymentWebhook.incrementAttempt();

        if (paymentWebhook.getAttemptCount() >= MAX_WEBHOOK_ATTEMPTS) {
            paymentWebhook.markFailed();
        }
    }
}
