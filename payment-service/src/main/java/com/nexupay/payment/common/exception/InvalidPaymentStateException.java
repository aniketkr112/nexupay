package com.nexupay.payment.common.exception;

import com.nexupay.payment.common.enums.PaymentStatus;

public class InvalidPaymentStateException extends RuntimeException {
  public InvalidPaymentStateException(PaymentStatus message) {
    super("Payment cannot be processed because current status is "+message);
  }
}
