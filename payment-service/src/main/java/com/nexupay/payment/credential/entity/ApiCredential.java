package com.nexupay.payment.credential.entity;

import com.nexupay.payment.credential.enums.CredentialStatus;
import com.nexupay.payment.credential.enums.Environment;
import com.nexupay.payment.merchant.entity.Merchant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_credential")
public class ApiCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "credential_id",nullable = false,unique = true)
    private String credentialId;
    @ManyToOne
    @JoinColumn(name = "merchant_id",nullable = false)
    private Merchant merchant;
    @Column(name = "api_key",nullable = false,unique = true)
    private String apiKey;
    @Column(name = "secret_key_hash",nullable = false)
    private String secretKeyHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "environment",nullable = false)
    private Environment environment;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private CredentialStatus status;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    public ApiCredential() {
    }

    public ApiCredential(String credentialId, Merchant merchant, String apiKey, String secretKeyHash, Environment environment, CredentialStatus status) {
        this.credentialId = credentialId;
        this.merchant = merchant;
        this.apiKey = apiKey;
        this.secretKeyHash = secretKeyHash;
        this.environment = environment;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreadentialId() {
        return credentialId;
    }

    public void setCreadentialId(String creadentialId) {
        this.credentialId = creadentialId;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKeyHash() {
        return secretKeyHash;
    }

    public void setSecretKeyHash(String secretKeyHash) {
        this.secretKeyHash = secretKeyHash;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public CredentialStatus getStatus() {
        return status;
    }

    public void setStatus(CredentialStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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


