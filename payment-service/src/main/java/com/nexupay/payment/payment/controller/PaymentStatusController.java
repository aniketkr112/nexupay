package com.nexupay.payment.payment.controller;

import com.nexupay.payment.payment.dto.response.PaymentStatusResponse;
import com.nexupay.payment.payment.service.PaymentStatusService;
import com.nexupay.payment.security.CurrentMerchantProvider;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentStatusController {

    private final PaymentStatusService paymentStatusService;
    private final CurrentMerchantProvider currentMerchantProvider;


    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable String paymentId) {

        AuthenticatedMerchant merchant =
                currentMerchantProvider.getCurrentMerchant();

        PaymentStatusResponse response =
                paymentStatusService.getPaymentStatus(
                        merchant,
                        paymentId
                );

        return ResponseEntity.ok(response);
    }
}