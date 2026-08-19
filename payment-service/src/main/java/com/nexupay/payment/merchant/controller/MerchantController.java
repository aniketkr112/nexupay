package com.nexupay.payment.merchant.controller;

import com.nexupay.payment.merchant.dto.request.CreateMerchantRequest;
import com.nexupay.payment.merchant.dto.request.UpdateWebhookRequest;
import com.nexupay.payment.merchant.dto.response.CreateMerchantResponse;
import com.nexupay.payment.merchant.service.MerchantService;
import com.nexupay.payment.security.CurrentMerchantProvider;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {
    private final MerchantService merchantService;
    private final CurrentMerchantProvider currentMerchantProvider;

    @PostMapping
    public ResponseEntity<CreateMerchantResponse> createMerchant( @Valid @RequestBody CreateMerchantRequest request){
        CreateMerchantResponse response = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/webhook")
    public ResponseEntity<Void> updateWebhook(
            @Valid @RequestBody UpdateWebhookRequest request) {

        AuthenticatedMerchant merchant =
                currentMerchantProvider.getCurrentMerchant();

        merchantService.updateWebhookUrl(merchant, request);

        return ResponseEntity.noContent().build();
    }
}
