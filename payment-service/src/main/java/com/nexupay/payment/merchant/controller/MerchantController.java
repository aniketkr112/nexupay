package com.nexupay.payment.merchant.controller;

import com.nexupay.payment.merchant.dto.request.CreateMerchantRequest;
import com.nexupay.payment.merchant.dto.response.CreateMerchantResponse;
import com.nexupay.payment.merchant.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {
    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    public ResponseEntity<CreateMerchantResponse> createMerchant( @Valid @RequestBody CreateMerchantRequest request){
        CreateMerchantResponse response = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
