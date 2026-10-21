package com.nexupay.payment.common.exception;

public class CheckoutAccessDeniedException extends RuntimeException {
    public CheckoutAccessDeniedException() {
        super("Valid checkout access is required");
    }
}
