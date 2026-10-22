package com.nexupay.payment.credential.entity;

import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.common.enums.Environment;
import com.nexupay.payment.merchant.entity.Merchant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "api_credential")
public class ApiCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "credential_id",nullable = false,unique = true)
    private String credentialId;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
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

    /** Used for credentials created during merchant registration. */
    public ApiCredential(
            String credentialId,
            Merchant merchant,
            String apiKey,
            String secretKeyHash,
            Environment environment,
            CredentialStatus status) {
        this(credentialId, "Initial credential", merchant, apiKey, secretKeyHash, environment, status);
    }

    public ApiCredential(
            String credentialId,
            String name,
            Merchant merchant,
            String apiKey,
            String secretKeyHash,
            Environment environment,
            CredentialStatus status) {
        this.credentialId = credentialId;
        this.name = name;
        this.merchant = merchant;
        this.apiKey = apiKey;
        this.secretKeyHash = secretKeyHash;
        this.environment = environment;
        this.status = status;
    }

    public void revoke() {
        this.status = CredentialStatus.REVOKED;
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


