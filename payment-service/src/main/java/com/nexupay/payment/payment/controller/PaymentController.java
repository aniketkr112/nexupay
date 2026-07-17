package com.nexupay.payment.payment.controller;

import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.dto.response.PaymentResponse;
import com.nexupay.payment.payment.service.PaymentService;
import com.nexupay.payment.security.CurrentMerchantProvider;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final CurrentMerchantProvider currentMerchantProvider;

    public PaymentController(PaymentService paymentService, CurrentMerchantProvider currentMerchantProvider) {
        this.paymentService = paymentService;
        this.currentMerchantProvider = currentMerchantProvider;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request){

        AuthenticatedMerchant merchant =
                currentMerchantProvider.getCurrentMerchant();

        PaymentResponse response =
                paymentService.createPayment(merchant,request);

        return ResponseEntity.ok(response);
    }
}
