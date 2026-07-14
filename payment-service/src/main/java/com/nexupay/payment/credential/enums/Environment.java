package com.nexupay.payment.credential.enums;

public enum Environment {

    SANDBOX("pk_test_", "sk_test_"),
    PRODUCTION("pk_live_", "sk_live_");

    private final String apiKeyPrefix;
    private final String secretKeyPrefix;

    Environment(String apiKeyPrefix, String secretKeyPrefix) {
        this.apiKeyPrefix = apiKeyPrefix;
        this.secretKeyPrefix = secretKeyPrefix;
    }

    public String getApiKeyPrefix() {
        return apiKeyPrefix;
    }

    public String getSecretKeyPrefix() {
        return secretKeyPrefix;
    }
}