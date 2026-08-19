package com.nexupay.payment.webhook.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-merchant")
@Slf4j
public class FakeMerchantWebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody String payload) {

        log.info("Fake merchant received webhook: {}", payload);

        return ResponseEntity.ok().build();
    }
}