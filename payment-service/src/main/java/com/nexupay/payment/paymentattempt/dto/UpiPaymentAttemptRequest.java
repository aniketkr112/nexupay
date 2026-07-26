package com.nexupay.payment.paymentattempt.dto;

import jakarta.validation.constraints.NotBlank;

public class UpiPaymentAttemptRequest {
    @NotBlank
    private String paymentId;
    @NotBlank
    private String upiId;

    public UpiPaymentAttemptRequest() {
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
}
