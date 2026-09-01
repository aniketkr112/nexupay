package com.nexupay.payment.payment.entity;

import com.nexupay.payment.common.enums.PaymentWebhookStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_webhook",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_webhook_payment",
                        columnNames = {"payment_id"}
                )
        }
)
@Getter
public class PaymentWebhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payment_id",nullable = false)
    private String paymentId;
    @Column(name = "webhook_url",nullable = false)
    private String webhookUrl;
    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentWebhookStatus status;
    @Column(name = "attempt_count",nullable = false)
    private Integer attemptCount;
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    public static PaymentWebhook create(String paymentId,String webhookUrl) {

        PaymentWebhook paymentWebhook = new PaymentWebhook();
        paymentWebhook.paymentId = paymentId;
        paymentWebhook.webhookUrl = webhookUrl;
        paymentWebhook.status = PaymentWebhookStatus.PENDING;
        paymentWebhook.attemptCount = 0;
        return paymentWebhook;
    }

    public void markSuccess(){
        this.status = PaymentWebhookStatus.SUCCESS;
    }

    public void markFailed(){
        this.status = PaymentWebhookStatus.FAILED;
    }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void markProcessingStarted() {
        this.processingStartedAt = LocalDateTime.now();
    }

    public void clearProcessingStarted() {
        this.processingStartedAt = null;
    }

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
