package com.nexupay.payment.common.exception;

public class PaymentAttemptNotFoundException extends RuntimeException {
    public PaymentAttemptNotFoundException(String message) {
        super("Payment attempt not found with attemptId: "+message);
    }
}
