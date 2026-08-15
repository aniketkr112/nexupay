package com.nexupay.payment.payment.dto.webhook;

import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.payment.entity.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentWebhookPayload {
    private String paymentId;
    private String merchantOrderId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;

    public static PaymentWebhookPayload from(Payment payment) {
        PaymentWebhookPayload payload = new PaymentWebhookPayload();

        payload.paymentId = payment.getPaymentId();
        payload.merchantOrderId = payment.getMerchantOrderId();
        payload.status = payment.getStatus();
        payload.amount = payment.getAmount();
        payload.currency = payment.getCurrency();

        return payload;
    }
}
