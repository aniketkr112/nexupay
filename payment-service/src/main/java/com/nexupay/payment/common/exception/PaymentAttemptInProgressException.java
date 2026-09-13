package com.nexupay.payment.common.exception;

public class PaymentAttemptInProgressException extends RuntimeException {
    public PaymentAttemptInProgressException(String message) {
        super(message);
    }
}
