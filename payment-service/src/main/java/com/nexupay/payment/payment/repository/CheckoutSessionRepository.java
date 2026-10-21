package com.nexupay.payment.payment.repository;

import com.nexupay.payment.payment.entity.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {

    @Query("select session from CheckoutSession session join fetch session.payment " +
            "where session.tokenHash = :tokenHash and session.revoked = false")
    Optional<CheckoutSession> findActiveSessionWithPaymentByTokenHash(
            @Param("tokenHash") String tokenHash);
}
