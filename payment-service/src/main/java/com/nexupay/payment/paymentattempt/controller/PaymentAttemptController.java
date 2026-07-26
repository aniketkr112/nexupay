package com.nexupay.payment.paymentattempt.controller;

import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptRequest;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptResponse;
import com.nexupay.payment.paymentattempt.service.PaymentAttemptService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-attempts")
public class PaymentAttemptController {

    private final PaymentAttemptService paymentAttemptService;

    public PaymentAttemptController(PaymentAttemptService paymentAttemptService) {
        this.paymentAttemptService = paymentAttemptService;
    }

    @PostMapping("/upi")
    public UpiPaymentAttemptResponse createUpiPaymentAttempt(
            @Valid @RequestBody UpiPaymentAttemptRequest request) {

        return paymentAttemptService.createUpiPaymentAttempt(request);
    }
}
