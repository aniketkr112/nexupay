package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.constant.PaymentConstants;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.dto.response.PaymentResponse;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final IdGeneration idGeneration;

    public PaymentService(PaymentRepository paymentRepository, IdGeneration idGeneration) {
        this.paymentRepository = paymentRepository;
        this.idGeneration = idGeneration;
    }

    public PaymentResponse createPayment(AuthenticatedMerchant merchant, PaymentRequest request){

        Long merchantId = merchant.getId();
        String merchantOrderId = request.getMerchantOrderId();
        Optional<Payment> existingPayment = findExistingPayment(merchantId, merchantOrderId);

        if(existingPayment.isPresent()){
            log.info(
                    "Idempotent retry detected. MerchantId={}, MerchantOrderId={}, PaymentId={}",
                    merchantId,
                    merchantOrderId,
                    existingPayment.get().getPaymentId()
            );
            return toPaymentResponse(existingPayment.get());
        }
        String paymentId = idGeneration.generatePaymentId();

        Payment payment = Payment.create(
                merchant.getId(),
                paymentId,
                request,
                LocalDateTime.now().plus(PaymentConstants.PAYMENT_EXPIRY)
        );

        // Handles the rare race condition where two identical
        // payment requests are processed concurrently.
        try {
            Payment savedPayment = paymentRepository.save(payment);
            return toPaymentResponse(savedPayment);
        } catch (DataIntegrityViolationException ex) {

            log.info(
                    "Concurrent payment creation detected. MerchantId={}, MerchantOrderId={}",
                    merchantId,
                    merchantOrderId
            );

            Payment alreadyExistPayment =
                    findExistingPayment(merchantId,merchantOrderId)
                            .orElseThrow(()->ex);
            return toPaymentResponse(alreadyExistPayment);
        }
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

    private Optional<Payment> findExistingPayment(Long merchantId,String merchantOrderId){
        return paymentRepository
                .findByMerchantIdAndMerchantOrderId(merchantId, merchantOrderId);
    }
}
