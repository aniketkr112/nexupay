
package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.payment.repository.CheckoutSessionRepository;
import com.nexupay.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nexupay.payment.common.constant.PaymentConstants;
import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.dto.response.PaymentResponse;
import com.nexupay.payment.payment.entity.CheckoutSession;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCreationTransactionService {

    private final PaymentRepository paymentRepository;
    private final IdGeneration idGeneration;
    private final CheckoutTokenService checkoutTokenService;
    private final CheckoutSessionRepository checkoutSessionRepository;



    @Transactional
    public PaymentResponse createPayment(
            AuthenticatedMerchant merchant,
            PaymentRequest request
    ) {

        Long merchantId = merchant.getId();
        String merchantOrderId = request.getMerchantOrderId();

        // Check whether this payment already exists.
        Optional<Payment> existingPayment =
                paymentRepository.findByMerchantIdAndMerchantOrderId(
                        merchantId,
                        merchantOrderId
                );

        if (existingPayment.isPresent()) {

            Payment payment = paymentRepository
                    .findByPaymentIdForUpdate(
                            existingPayment.get().getPaymentId()
                    )
                    .orElseThrow();

            return toPaymentResponseWithCheckoutToken(payment);
        }

        // Create a new payment.
        String paymentId = idGeneration.generatePaymentId();

        Payment payment = Payment.create(
                merchantId,
                paymentId,
                request,
                LocalDateTime.now().plus(PaymentConstants.PAYMENT_EXPIRY)
        );

        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        return toPaymentResponseWithCheckoutToken(savedPayment);
    }

    private PaymentResponse toPaymentResponseWithCheckoutToken(Payment payment) {
        PaymentResponse response = toPaymentResponse(payment);

        // Only an open, unexpired payment can receive a new checkout session.
        if (payment.getStatus() == PaymentStatus.CREATED
                && payment.getExpiresAt().isAfter(LocalDateTime.now())) {
            // revoking the old active checkout session corresponding to this payment.
            checkoutSessionRepository.revokeActiveSessionsByPaymentId(
                    payment.getPaymentId()
            );

            String rawToken = checkoutTokenService.generateToken();
            String tokenHash = checkoutTokenService.hashToken(rawToken);

            CheckoutSession session = CheckoutSession.create(
                    payment,
                    tokenHash,
                    payment.getExpiresAt()
            );
            checkoutSessionRepository.save(session);

            // The raw token is returned once; only its hash is persisted.
            response.setCheckoutToken(rawToken);
            response.setCheckoutMessage("Checkout session created.");

        }

        return response;
    }

    private PaymentResponse toPaymentResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        if (!payment.getExpiresAt().isAfter(LocalDateTime.now())) {
            response.setCheckoutMessage("Payment has expired.");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            response.setCheckoutMessage("Payment has already been completed.");
        }else if (payment.getStatus() != PaymentStatus.CREATED) {
            response.setCheckoutMessage("Payment cannot be attempted.");
        }

        response.setPaymentId(payment.getPaymentId());
        response.setMerchantOrderId(payment.getMerchantOrderId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setExpiresAt(payment.getExpiresAt());

        return response;
    }


}
