package com.nexupay.payment.paymentattempt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpiPaymentAttemptRequest {
    @NotBlank
    private String paymentId;
    @NotBlank
    private String upiId;

    public UpiPaymentAttemptRequest() {
    }

}
