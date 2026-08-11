package com.nexupay.payment.payment.dto.response;

import com.nexupay.payment.common.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentStatusResponse {
    private String paymentId;
    private String merchantOrderId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
