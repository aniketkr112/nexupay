package com.nexupay.payment.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "checkout_session",
        indexes = {
                @Index(name = "idx_checkout_session_payment_id", columnList = "payment_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_checkout_session_token_hash", columnNames = "token_hash")
        }
)
public class CheckoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CheckoutSession() {
    }

    private CheckoutSession(
            Payment payment,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        this.payment = payment;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public static CheckoutSession create(
            Payment payment,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }
        if (tokenHash == null || !tokenHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Token hash must be a 64-character SHA-256 hex value");
        }
        if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Checkout session expiry must be in the future");
        }
        if (payment.getExpiresAt() != null && expiresAt.isAfter(payment.getExpiresAt())) {
            throw new IllegalArgumentException("Checkout session cannot outlive its payment");
        }

        return new CheckoutSession(payment, tokenHash, expiresAt);
    }

    public Long getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void revoke() {
        this.revoked = true;
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
