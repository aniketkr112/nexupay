package com.nexupay.payment.payment.service;

import com.nexupay.payment.payment.dto.response.PaymentStatusResponse;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;

public interface PaymentStatusService {

    PaymentStatusResponse getPaymentStatus(
            AuthenticatedMerchant merchant,
            String paymentId
    );

}
