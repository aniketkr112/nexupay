package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.exception.CheckoutAccessDeniedException;
import com.nexupay.payment.payment.entity.CheckoutSession;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.CheckoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckoutAuthorizationService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutTokenService checkoutTokenService;

    /** Validates the bearer token and confirms it belongs to the requested payment. */
    public Payment authorize(String paymentId, String authorizationHeader) {
        if (paymentId == null || paymentId.isBlank()
                || authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new CheckoutAccessDeniedException();
        }

        String rawToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (rawToken.isBlank()) {
            throw new CheckoutAccessDeniedException();
        }

        String tokenHash = checkoutTokenService.hashToken(rawToken);
        CheckoutSession session = checkoutSessionRepository
                .findActiveSessionWithPaymentByTokenHash(tokenHash)
                .orElseThrow(CheckoutAccessDeniedException::new);

        LocalDateTime now = LocalDateTime.now();
        Payment payment = session.getPayment();
        if (!paymentId.equals(payment.getPaymentId())
                || !session.getExpiresAt().isAfter(now)
                || payment.getExpiresAt() == null
                || !payment.getExpiresAt().isAfter(now)) {
            throw new CheckoutAccessDeniedException();
        }

        return payment;
    }
}
