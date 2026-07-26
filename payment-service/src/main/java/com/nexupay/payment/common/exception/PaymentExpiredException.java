package com.nexupay.payment.common.exception;

public class PaymentExpiredException extends RuntimeException {
    public PaymentExpiredException(String message) {
        super(message);
    }
}
