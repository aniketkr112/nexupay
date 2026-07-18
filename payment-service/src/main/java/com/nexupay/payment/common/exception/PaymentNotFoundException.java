package com.nexupay.payment.common.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super("Payment not found:"+message);
    }
}
