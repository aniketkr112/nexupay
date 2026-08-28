package com.nexupay.payment.refund.service.transaction;

import com.nexupay.payment.refund.enums.RefundStatus;
import com.nexupay.payment.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundClaimService {

    private final RefundRepository refundRepository;

    @Transactional
    public boolean claimRefund(Long refundId) {

        return refundRepository.claimRefund(
                refundId,
                RefundStatus.PENDING
        ) == 1;
    }
}