package com.nexupay.payment.refund.service;

import com.nexupay.payment.refund.dto.request.CreateRefundRequest;
import com.nexupay.payment.refund.dto.response.CreateRefundResponse;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;

public interface RefundService {
    CreateRefundResponse createRefund(
            AuthenticatedMerchant merchant,
            CreateRefundRequest request
    );
}
