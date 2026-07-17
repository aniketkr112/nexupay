package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.dto.response.PaymentResponse;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final IdGeneration idGeneration;

    public PaymentService(PaymentRepository paymentRepository, IdGeneration idGeneration) {
        this.paymentRepository = paymentRepository;
        this.idGeneration = idGeneration;
    }

    public PaymentResponse createPayment(AuthenticatedMerchant merchant, PaymentRequest request){

        Optional<Payment> existingPayment = paymentRepository
                .findByMerchantIdAndMerchantOrderId(merchant.getMerchantId(), request.getMerchantOrderId());

        if(existingPayment.isPresent()){
            return toPaymentResponse(existingPayment.get());
        }
        String paymentId = idGeneration.generatePaymentId();
        return null;
    }

    private PaymentResponse toPaymentResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setPaymentId(payment.getPaymentId());
        response.setMerchantOrderId(payment.getMerchantOrderId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setExpiresAt(payment.getExpiresAt());

        return response;
    }
}
