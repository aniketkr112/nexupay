package com.nexupay.payment.paymentattempt.repository;

import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.paymentattempt.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt,Long> {
    @Query("""
        SELECT MAX(pa.attemptNumber)
        FROM PaymentAttempt pa
        WHERE pa.payment = :payment
    """)
    Optional<Integer> findMaxAttemptNumber(@Param("payment") Payment payment);

    Optional<PaymentAttempt> findByAttemptId(String attemptId);

}
