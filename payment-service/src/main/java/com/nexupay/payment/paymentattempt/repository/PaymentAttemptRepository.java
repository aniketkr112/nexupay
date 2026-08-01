package com.nexupay.payment.paymentattempt.repository;

import com.nexupay.payment.common.enums.PaymentAttemptStatus;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.paymentattempt.entity.PaymentAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt,Long> {
    @Query("""
        SELECT MAX(pa.attemptNumber)
        FROM PaymentAttempt pa
        WHERE pa.payment = :payment
    """)
    Optional<Integer> findMaxAttemptNumber(@Param("payment") Payment payment);

    Optional<PaymentAttempt> findByAttemptId(String attemptId);

    List<PaymentAttempt> findByStatusAndCreatedAtBefore(
            PaymentAttemptStatus status,
            LocalDateTime createdAt,
            Pageable pageable
    );

}
