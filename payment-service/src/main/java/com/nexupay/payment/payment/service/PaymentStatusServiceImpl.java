package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.exception.MerchantAccessDeniedException;
import com.nexupay.payment.common.exception.PaymentNotFoundException;
import com.nexupay.payment.payment.dto.response.PaymentStatusResponse;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentStatusServiceImpl implements PaymentStatusService{

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentStatusResponse getPaymentStatus(
            AuthenticatedMerchant merchant, String paymentId) {

        Payment payment = paymentRepository
                .findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if(!Objects.equals(payment.getMerchantId(), merchant.getId())){
            throw new MerchantAccessDeniedException("Payment not found by merchant");
        }

        return buildResponse(payment);
    }

    private PaymentStatusResponse buildResponse(Payment payment){
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setMerchantOrderId(payment.getMerchantOrderId());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setCreatedAt(payment.getCreatedAt());
        response.setExpiresAt(payment.getExpiresAt());
        return response;
    }
}
