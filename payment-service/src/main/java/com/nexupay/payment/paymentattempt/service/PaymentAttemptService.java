package com.nexupay.payment.paymentattempt.service;

import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptResponse;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptRequest;

public interface PaymentAttemptService {
    UpiPaymentAttemptResponse createUpiPaymentAttempt(UpiPaymentAttemptRequest request);
}
