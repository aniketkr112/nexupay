package com.nexupay.payment.common.exception;

public class PaymentAttemptNotFoundException extends RuntimeException {
  public PaymentAttemptNotFoundException(String message) {
    super(message);
  }
}
