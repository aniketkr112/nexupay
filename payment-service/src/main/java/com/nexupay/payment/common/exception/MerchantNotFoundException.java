package com.nexupay.payment.common.exception;

public class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(Long message) {
        super("Merchant not found:"+message);
    }
}
