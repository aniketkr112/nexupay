package com.nexupay.payment.refund.repository;

import com.nexupay.payment.refund.entity.Refund;
import com.nexupay.payment.refund.enums.RefundStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

    List<Refund> findByStatus(RefundStatus status, Pageable pageable);

    @Query("""
    SELECT r
    FROM Refund r
    WHERE r.status = :status
      AND (
          r.processingStartedAt IS NULL
          OR r.processingStartedAt < :expirationTime
      )
    ORDER BY r.createdAt ASC
""")
    List<Refund> findUnclaimedRefunds(
            @Param("status") RefundStatus status,
            @Param("expirationTime") LocalDateTime expirationTime,
            Pageable pageable
    );

    @Modifying
    @Query("""
    UPDATE Refund r
    SET r.processingStartedAt = CURRENT_TIMESTAMP
    WHERE r.id = :id
      AND r.status = :status
      AND (
          r.processingStartedAt IS NULL
          OR r.processingStartedAt < :expirationTime
      )
""")
    int claimRefund(
            @Param("id") Long id,
            @Param("status") RefundStatus status,
            @Param("expirationTime") LocalDateTime expirationTime
    );
}