package com.nexupay.payment.paymentattempt.entity;

import com.nexupay.payment.bank.enums.BankFailureReason;
import com.nexupay.payment.common.enums.PaymentAttemptStatus;
import com.nexupay.payment.common.enums.PaymentMethod;
import com.nexupay.payment.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_attempt",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_attempt_number",
                        columnNames = {
                                "payment_id",
                                "attempt_number"
                        }
                )
        }
)
@Getter
@NoArgsConstructor
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false, unique = true)
    private String attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentAttemptStatus status;

    @Column(name = "failure_reason")
    @Enumerated(EnumType.STRING)
    private BankFailureReason failureReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "bank_reference_id")
    private String bankReferenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PaymentAttempt(
            String attemptId,
            Payment payment,
            Integer attemptNumber,
            PaymentMethod paymentMethod) {

        this.attemptId = attemptId;
        this.payment = payment;
        this.attemptNumber = attemptNumber;
        this.paymentMethod = paymentMethod;

        this.status = PaymentAttemptStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    public void markSuccess(String bankReferenceId){
        this.status = PaymentAttemptStatus.SUCCESS;
        this.bankReferenceId = bankReferenceId;
        this.failureReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(BankFailureReason reason){
        this.status = PaymentAttemptStatus.FAILED;
        this.failureReason = reason;
        this.bankReferenceId = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markUnknown(BankFailureReason reason){
        this.status = PaymentAttemptStatus.UNKNOWN;
        this.failureReason = reason;
        this.bankReferenceId = null;
        this.updatedAt = LocalDateTime.now();
    }
}