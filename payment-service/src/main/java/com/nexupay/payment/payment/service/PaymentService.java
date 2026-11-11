package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.constant.PaymentConstants;
import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.dto.response.PaymentResponse;
import com.nexupay.payment.payment.entity.CheckoutSession;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.CheckoutSessionRepository;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdGeneration idGeneration;
    private final CheckoutTokenService checkoutTokenService;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final PaymentCreationTransactionService paymentCreationTransactionService;



    public PaymentResponse createPayment(
            AuthenticatedMerchant merchant,
            PaymentRequest request
    ) {
        try {
            return paymentCreationTransactionService.createPayment(
                    merchant,
                    request
            );
        } catch (DataIntegrityViolationException ex) {

            if (!isMerchantOrderDuplicate(ex)) {
                throw ex;
            }

            log.info(
                    "Concurrent payment creation detected. MerchantId={}, MerchantOrderId={}",
                    merchant.getId(),
                    request.getMerchantOrderId()
            );

            return paymentCreationTransactionService.createPayment(
                    merchant,
                    request
            );
        }
    }

    private boolean isMerchantOrderDuplicate(
            DataIntegrityViolationException ex
    ) {
        Throwable cause = ex;

        while (cause != null) {

            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                return "uk_payment_merchant_order".equals(
                        violation.getConstraintName()
                );
            }

            cause = cause.getCause();
        }

        return false;
    }
}
