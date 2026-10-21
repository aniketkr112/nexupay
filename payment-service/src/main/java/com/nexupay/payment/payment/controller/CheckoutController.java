package com.nexupay.payment.payment.controller;

import com.nexupay.payment.payment.dto.response.CheckoutResponse;
import com.nexupay.payment.payment.service.CheckoutService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/{paymentId}")
    public CheckoutResponse getCheckout(
            @PathVariable String paymentId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return checkoutService.getCheckout(paymentId, authorizationHeader);
    }
}