package com.nexupay.payment.refund.entity;

import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.common.exception.InvalidPaymentStateException;
import com.nexupay.payment.common.exception.PaymentExpiredException;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.refund.dto.request.CreateRefundRequest;
import com.nexupay.payment.refund.enums.BankRefundFailureReason;
import com.nexupay.payment.refund.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "refund",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refund_merchant_refund",
                        columnNames = {
                                "merchant_refund_id",
                                "payment_id"
                        }
                )
        }
)
@Getter
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "refund_id",nullable = false,unique = true)
    private String refundId;
    @Column(name = "payment_id",nullable = false)
    private String paymentId;
    @Column(name = "merchant_refund_id",nullable = false)
    private String merchantRefundId;
    @Column(name = "amount",nullable = false)
    private BigDecimal amount;
    @Column(name = "currency",nullable = false)
    private String currency;
    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status;
    @Column(name = "bank_reference_id")
    private String bankReferenceId;
    @Column(name = "failure_reason")
    @Enumerated(EnumType.STRING)
    private BankRefundFailureReason failureReason;
    @Column(name = "bank_submission_attempted", nullable = false)
    private boolean bankSubmissionAttempted;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;


    public static Refund create(
            String refundId,
            String merchantRefundId,
            Payment payment,
            CreateRefundRequest request
    ) {

        Refund refund = new Refund();

        refund.refundId = refundId;
        refund.paymentId = payment.getPaymentId();
        refund.merchantRefundId = merchantRefundId;
        refund.amount = request.getAmount();
        refund.currency = payment.getCurrency();
        refund.status = RefundStatus.PENDING;
        refund.bankSubmissionAttempted = false;

        return refund;
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

    public void markSuccessful(String bankReferenceId){
        this.bankReferenceId = bankReferenceId;
        this.status = RefundStatus.SUCCESS;
    }
    public void markBankSubmissionAttempted() {
        this.bankSubmissionAttempted = true;
    }
    public void markFailed(){
        this.status = RefundStatus.FAILED;
    }

}
