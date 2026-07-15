package com.nexupay.payment.credential.service;

import com.nexupay.payment.common.cryptography.SecretKeyHasher;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CredentialAuthenticationService {

    private final ApiCredentialRepository apiCredentialRepository;
    private final SecretKeyHasher secretKeyHasher;


    public CredentialAuthenticationService(
            ApiCredentialRepository apiCredentialRepository, SecretKeyHasher secretKeyHasher) {

        this.apiCredentialRepository = apiCredentialRepository;

        this.secretKeyHasher = secretKeyHasher;
    }

    public AuthenticatedMerchant authenticate(String apiKey, String secretKey) {
        Optional<ApiCredential> credential =
                apiCredentialRepository.findByApiKey(apiKey);

        if (credential.isEmpty()) {
            return null;
        }
        ApiCredential apiCredential = credential.get();
        boolean validSecret = secretKeyHasher.match(secretKey,apiCredential.getSecretKeyHash());
        if (!validSecret) {
            return null;
        }
        if (apiCredential.getStatus() != CredentialStatus.ACTIVE) {
            return null;
        }
        return new AuthenticatedMerchant(apiCredential.getMerchant().getId(),apiCredential.getMerchant().getMerchantId());
    }
}
