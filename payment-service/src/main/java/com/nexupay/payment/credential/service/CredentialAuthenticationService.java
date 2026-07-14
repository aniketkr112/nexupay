package com.nexupay.payment.credential.service;

import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CredentialAuthenticationService {

    private final ApiCredentialRepository apiCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialAuthenticationService(
            ApiCredentialRepository apiCredentialRepository,
            PasswordEncoder passwordEncoder) {

        this.apiCredentialRepository = apiCredentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean authenticate(String apiKey, String secretKey) {
        Optional<ApiCredential> credential =
                apiCredentialRepository.findByApiKey(apiKey);

        if (credential.isEmpty()) {
            return false;
        }
        ApiCredential apiCredential = credential.get();
        boolean validSecret = passwordEncoder.matches(secretKey,apiCredential.getSecretKeyHash());
        if (!validSecret) {
            return false;
        }
        if (apiCredential.getStatus() != CredentialStatus.ACTIVE) {
            return false;
        }
        return true;
    }
}
