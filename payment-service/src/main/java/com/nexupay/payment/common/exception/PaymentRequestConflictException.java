package com.nexupay.payment.common.exception;

public class PaymentRequestConflictException extends RuntimeException {
  public PaymentRequestConflictException(String message) {
    super(message);
  }
}
