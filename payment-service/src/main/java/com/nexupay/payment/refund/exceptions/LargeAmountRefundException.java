package com.nexupay.payment.refund.exceptions;

public class LargeAmountRefundException extends RuntimeException {
    public LargeAmountRefundException(String message) {
        super(message);
    }
}
