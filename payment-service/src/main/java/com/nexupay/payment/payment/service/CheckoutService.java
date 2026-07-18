package com.nexupay.payment.payment.service;

import com.nexupay.payment.payment.dto.response.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse getCheckout(String paymentId);
}
