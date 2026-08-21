package com.nexupay.payment.refund.controller;

import com.nexupay.payment.refund.dto.request.CreateRefundRequest;
import com.nexupay.payment.refund.dto.response.CreateRefundResponse;
import com.nexupay.payment.refund.service.RefundService;
import com.nexupay.payment.security.CurrentMerchantProvider;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/refunds")
public class RefundController {
    private final RefundService refundService;
    private final CurrentMerchantProvider currentMerchantProvider;

    public RefundController(RefundService refundService, CurrentMerchantProvider currentMerchantProvider) {
        this.refundService = refundService;
        this.currentMerchantProvider = currentMerchantProvider;
    }


    @PostMapping
    public ResponseEntity<CreateRefundResponse> createRefund(@Valid @RequestBody CreateRefundRequest request){

        AuthenticatedMerchant merchant = currentMerchantProvider
                .getCurrentMerchant();

        CreateRefundResponse response = refundService
                .createRefund(merchant,request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
