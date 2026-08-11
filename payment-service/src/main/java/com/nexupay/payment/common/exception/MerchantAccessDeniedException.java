package com.nexupay.payment.common.exception;

public class MerchantAccessDeniedException extends RuntimeException {
    public MerchantAccessDeniedException(String message) {
        super(message);
    }
}
