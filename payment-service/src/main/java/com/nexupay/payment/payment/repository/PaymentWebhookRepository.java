package com.nexupay.payment.payment.repository;

import com.nexupay.payment.common.enums.PaymentWebhookStatus;
import com.nexupay.payment.payment.entity.PaymentWebhook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook,Long> {

    List<PaymentWebhook> findByStatus(
            PaymentWebhookStatus status,
            Pageable pageable
    );

    @Modifying
    @Query("""
    UPDATE PaymentWebhook w
       SET w.processingStartedAt = CURRENT_TIMESTAMP
     WHERE w.id = :id
       AND w.status = :status
       AND (
            w.processingStartedAt IS NULL
            OR w.processingStartedAt < :expiryTime
       )
""")
    int claimWebhook(
            @Param("id") Long id,
            @Param("status") PaymentWebhookStatus status,
            @Param("expiryTime") LocalDateTime expiryTime
    );
}
