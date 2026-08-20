package com.nexupay.payment.refund.exceptions;

public class PaymentNotEligibleForRefundException extends RuntimeException {
    public PaymentNotEligibleForRefundException(String message) {
        super(message);
    }
}
