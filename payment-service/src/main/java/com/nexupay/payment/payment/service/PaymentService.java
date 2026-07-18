package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.constant.PaymentConstants;
import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.dto.response.PaymentResponse;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                .findByMerchantIdAndMerchantOrderId(merchant.getId(), request.getMerchantOrderId());

        if(existingPayment.isPresent()){
            return toPaymentResponse(existingPayment.get());
        }
        String paymentId = idGeneration.generatePaymentId();

        Payment payment = createPayment(merchant,request,paymentId);

        // This is for race condition when same two request come at the same time
        try {
            Payment savedPayment = paymentRepository.save(payment);
            return toPaymentResponse(savedPayment);
        } catch (DataIntegrityViolationException ex) {
            // TODO(TECH-DEBT):
            // Handle race condition caused by
            // uk_payment_merchant_order unique constraint.

            throw ex;
        }
    }

    private Payment createPayment(AuthenticatedMerchant merchant,PaymentRequest request,String paymentId){
        Payment payment = new Payment();

        payment.setPaymentId(paymentId);
        payment.setMerchantId(merchant.getId());
        payment.setMerchantOrderId(request.getMerchantOrderId());

        payment.setCustomerName(request.getCustomerName());
        payment.setCustomerEmail(request.getCustomerEmail());
        payment.setCustomerPhone(request.getCustomerPhone());

        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setExpireAt(LocalDateTime.now().plus(PaymentConstants.PAYMENT_EXPIRY));

        return payment;
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
