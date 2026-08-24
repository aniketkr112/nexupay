package com.nexupay.payment.bank.entity;

import com.nexupay.payment.bank.dto.BankRefundRequest;
import com.nexupay.payment.bank.enums.BankRefundStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bank_refund",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bank_refund_refund_id",
                        columnNames = "refund_id"
                )
        }
)
@Getter
public class BankRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refund_id", nullable = false, unique = true)
    private String refundId;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "bank_reference_id", nullable = false, unique = true)
    private String bankReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankRefundStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static BankRefund create(BankRefundRequest request,String bankReferenceId,BankRefundStatus bankRefundStatus){
        BankRefund bankRefund = new BankRefund();

        bankRefund.refundId = (request.getRefundId());
        bankRefund.paymentId = (request.getPaymentId());
        bankRefund.amount = (request.getAmount());
        bankRefund.currency = (request.getCurrency());
        bankRefund.bankReferenceId = (bankReferenceId);
        bankRefund.status = bankRefundStatus;
        return bankRefund;
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