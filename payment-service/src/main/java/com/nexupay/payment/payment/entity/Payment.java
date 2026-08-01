package com.nexupay.payment.payment.entity;

import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.common.exception.InvalidPaymentStateException;
import com.nexupay.payment.common.exception.PaymentExpiredException;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_merchant_order",
                        columnNames = {
                                "merchant_id",
                                "merchant_order_id"
                        }
                )
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payment_id",nullable = false,unique = true)
    private String paymentId;
    @Column(name = "merchant_id",nullable = false)
    private Long merchantId;
    @Column(name = "merchant_order_id",nullable = false)
    private String merchantOrderId;
    @Column(name = "customer_name",nullable = true)
    private String customerName;
    @Column(length = 255, name = "customer_email",nullable = true)
    private String customerEmail;
    @Column(length = 20, name = "customer_phone",nullable = true)
    private String customerPhone;
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(length = 3, name = "currency",nullable = false)
    private String currency;
    @Column(name = "expires_at",nullable = false)
    private LocalDateTime expiresAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private PaymentStatus status;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;


    public static Payment create(
            Long merchantId,
            String paymentId,
            PaymentRequest request,
            LocalDateTime expiresAt
    ) {

        Payment payment = new Payment();

        payment.paymentId = paymentId;
        payment.merchantId = merchantId;
        payment.merchantOrderId = request.getMerchantOrderId();

        payment.customerName = request.getCustomerName();
        payment.customerEmail = request.getCustomerEmail();
        payment.customerPhone = request.getCustomerPhone();

        payment.amount = request.getAmount();
        payment.currency = request.getCurrency();

        payment.status = PaymentStatus.CREATED;
        payment.expiresAt = expiresAt;

        return payment;
    }

    protected Payment(){

    }

    public Long getId() {
        return id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Long getMerchantId() {
        return merchantId;
    }


    public String getMerchantOrderId() {
        return merchantOrderId;
    }


    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }


    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }


    public PaymentStatus getStatus() {
        return status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    public void markSuccessful(){
        this.status = PaymentStatus.SUCCESS;
    }
    public void ensurePaymentCanBeAttempted() {

        if (status != PaymentStatus.CREATED) {
            throw new InvalidPaymentStateException(status);
        }

        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new PaymentExpiredException("Payment is expired.");
        }
    }

}
