package com.nexupay.payment.payment.controller;

import com.nexupay.payment.payment.dto.response.CheckoutResponse;
import com.nexupay.payment.payment.service.CheckoutService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/{paymentId}")
    public CheckoutResponse getCheckout(@PathVariable String paymentId) {

        return checkoutService.getCheckout(paymentId);
    }
}