package com.nexupay.payment.merchant.entity;

import com.nexupay.payment.merchant.dto.request.CreateMerchantRequest;
import jakarta.persistence.*;

import com.nexupay.payment.common.enums.MerchantStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant")
@Getter
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "merchant_id",nullable = false,unique = true)
    private String merchantId;
    @Column(name = "business_name",nullable = false)
    private String businessName;
    @Column(name = "email",nullable = false,unique = true)
    private String email;
    @Column(name = "phone")
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private MerchantStatus status;
    @Column(name = "webhook_url",nullable = true)
    private String webhookUrl;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    protected Merchant() {
    }

    public static Merchant create(
            String merchantId,
            CreateMerchantRequest request
    ) {

        Merchant merchant = new Merchant();

        merchant.merchantId = merchantId;
        merchant.businessName = request.getBusinessName();
        merchant.email = request.getEmail();
        merchant.phone = request.getPhone();
        merchant.status = MerchantStatus.ACTIVE;

        return merchant;
    }

    public void updateWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
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
