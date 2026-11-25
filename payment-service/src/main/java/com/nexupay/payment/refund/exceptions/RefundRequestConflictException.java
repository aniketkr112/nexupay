package com.nexupay.payment.refund.exceptions;

public class RefundRequestConflictException extends RuntimeException {
    public RefundRequestConflictException(String message) {
        super(message);
    }
}
