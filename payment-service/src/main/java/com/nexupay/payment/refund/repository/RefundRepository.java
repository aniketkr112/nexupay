package com.nexupay.payment.refund.repository;

import com.nexupay.payment.refund.entity.Refund;
import com.nexupay.payment.refund.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByRefundId(String refundId);

    Optional<Refund> findByPaymentIdAndMerchantRefundId(
            String paymentId,
            String merchantRefundId
    );

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE r.paymentId = :paymentId
        AND r.status IN :statuses
    """)
    BigDecimal sumRefundAmountByPaymentIdAndStatusIn(
            @Param("paymentId") String paymentId,
            @Param("statuses") Collection<RefundStatus> statuses
    );
}