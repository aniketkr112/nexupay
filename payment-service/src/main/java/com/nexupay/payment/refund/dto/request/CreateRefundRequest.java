package com.nexupay.payment.refund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateRefundRequest {
    @NotBlank(message = "Payment id is required")
    @Size(max = 50, message = "Payment id must not exceed 50 characters")
    private String paymentId;
    @NotBlank(message = "Merchant refund id is required")
    @Size(max = 100, message = "Merchant refund id must not exceed 100 characters")
    private String merchantRefundId;
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;
}
