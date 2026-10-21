package com.nexupay.payment.payment.dto.response;

import com.nexupay.payment.common.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class PaymentResponse {
    private String paymentId;

    private String merchantOrderId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private LocalDateTime expiresAt;

    // Returned to the merchant so it can securely deliver checkout access to the customer.
    private String checkoutToken;

}
