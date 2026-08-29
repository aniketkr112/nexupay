package com.nexupay.payment.refund.service.transaction;

import com.nexupay.payment.refund.enums.RefundStatus;
import com.nexupay.payment.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundClaimService {

    private final RefundRepository refundRepository;

    @Transactional
    public boolean claimRefund(Long refundId) {

        LocalDateTime expirationTime =
                LocalDateTime.now().minusMinutes(5);

        return refundRepository.claimRefund(
                refundId,
                RefundStatus.PENDING,
                expirationTime
        ) == 1;
    }
}