package com.nexupay.payment.payment.repository;

import com.nexupay.payment.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByMerchantIdAndMerchantOrderId(
            Long merchantId,
            String merchantOrderId
    );

    Optional<Payment> findByPaymentId(String paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT p
       FROM Payment p
       WHERE p.paymentId = :paymentId
       """)
    Optional<Payment> findByPaymentIdForUpdate(@Param("paymentId") String paymentId);
}
