package com.nexupay.payment.credential.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CredentialSummaryResponse {

    private final String credentialId;
    private final String name;
    private final String apiKeyPrefix;
    private final String environment;
    private final String status;
    private final LocalDateTime createdAt;

    public CredentialSummaryResponse(
            String credentialId,
            String name,
            String apiKeyPrefix,
            String environment,
            String status,
            LocalDateTime createdAt) {
        this.credentialId = credentialId;
        this.name = name;
        this.apiKeyPrefix = apiKeyPrefix;
        this.environment = environment;
        this.status = status;
        this.createdAt = createdAt;
    }

}