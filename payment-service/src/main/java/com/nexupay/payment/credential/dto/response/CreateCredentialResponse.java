package com.nexupay.payment.credential.dto.response;

import java.time.LocalDateTime;

public class CreateCredentialResponse {
    private String credentialId;
    private String name;
    private String apiKey;
    private String secretKey;
    private String environment;
    private String status;
    private LocalDateTime createdAt;

    public CreateCredentialResponse() {
    }

    public CreateCredentialResponse(
            String credentialId,
            String name,
            String apiKey,
            String secretKey,
            String environment,
            String status,
            LocalDateTime createdAt) {
        this.credentialId = credentialId;
        this.name = name;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.environment = environment;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getCredentialId() { return credentialId; }
    public String getName() { return name; }
    public String getApiKey() { return apiKey; }
    public String getSecretKey() { return secretKey; }
    public String getEnvironment() { return environment; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
