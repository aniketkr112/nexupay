package com.nexupay.payment.refund.scheduler;

import com.nexupay.payment.refund.service.RefundProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundScheduler {

    private final RefundProcessor refundProcessor;

    @Scheduled(fixedDelay = 10_000)
    public void processPendingRefunds() {
        refundProcessor.processPendingRefunds();
    }
}