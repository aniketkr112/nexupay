package com.nexupay.payment.paymentattempt.dto;

import com.nexupay.payment.common.enums.PaymentStatus;
import lombok.Getter;

@Getter
public class UpiPaymentAttemptResponse {

    private String paymentId;

    private String attemptId;

    private PaymentStatus status;

    private String message;

    public UpiPaymentAttemptResponse(String paymentId, String attemptId, PaymentStatus status, String message) {
        this.paymentId = paymentId;
        this.attemptId = attemptId;
        this.status = status;
        this.message = message;
    }
}
