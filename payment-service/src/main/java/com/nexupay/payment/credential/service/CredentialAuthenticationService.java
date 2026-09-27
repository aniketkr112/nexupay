package com.nexupay.payment.credential.service;

import com.nexupay.payment.common.cryptography.SecretKeyHasher;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import com.nexupay.payment.security.cache.AuthenticationCacheService;
import com.nexupay.payment.security.cache.AuthenticationLockService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class CredentialAuthenticationService {

    private final ApiCredentialRepository apiCredentialRepository;
    private final SecretKeyHasher secretKeyHasher;
    private final AuthenticationCacheService authenticationCacheService;
    private final AuthenticationLockService authenticationLockService;


    public CredentialAuthenticationService(
            ApiCredentialRepository apiCredentialRepository,
            SecretKeyHasher secretKeyHasher,
            AuthenticationCacheService authenticationCacheService,
            AuthenticationLockService authenticationLockService) {

        this.apiCredentialRepository = apiCredentialRepository;
        this.secretKeyHasher = secretKeyHasher;
        this.authenticationCacheService = authenticationCacheService;
        this.authenticationLockService = authenticationLockService;
    }

    public AuthenticatedMerchant authenticate(String apiKey, String secretKey) {

        // Redis check
        AuthenticatedMerchant cachedMerchant =
                authenticationCacheService.get(apiKey, secretKey);

        if (cachedMerchant != null) {
            return cachedMerchant;
        }

        String lockKey = "nexupay:auth:lock:" + apiKey;
        String lockValue = UUID.randomUUID().toString();

        boolean lockAcquired = authenticationLockService.acquireLock(
                lockKey,
                lockValue,
                Duration.ofSeconds(5)
        );

        if (!lockAcquired) {
            // Another request is currently rebuilding the cache.
            // Give it a short amount of time to finish.
            for (int i = 0; i < 10; i++) {

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                cachedMerchant =
                        authenticationCacheService.get(apiKey, secretKey);

                if (cachedMerchant != null) {
                    return cachedMerchant;
                }
            }

            // Cache still unavailable after bounded waiting.
            // Do not wait indefinitely.
            return null;
        }

        try {
            // 2. IMPORTANT: double-check Redis after acquiring lock
            cachedMerchant =
                    authenticationCacheService.get(apiKey, secretKey);

            if (cachedMerchant != null) {
                return cachedMerchant;
            }

            // 3. Only the lock owner reaches DB + BCrypt
            Optional<ApiCredential> credential =
                    apiCredentialRepository.findByApiKey(apiKey);

            if (credential.isEmpty()) {
                return null;
            }

            ApiCredential apiCredential = credential.get();

            boolean validSecret =
                    secretKeyHasher.match(
                            secretKey,
                            apiCredential.getSecretKeyHash()
                    );

            if (!validSecret) {
                return null;
            }

            if (apiCredential.getStatus() != CredentialStatus.ACTIVE) {
                return null;
            }

            AuthenticatedMerchant authenticatedMerchant =
                    new AuthenticatedMerchant(
                            apiCredential.getMerchant().getId(),
                            apiCredential.getMerchant().getMerchantId()
                    );

            // 4. Populate cache
            authenticationCacheService.put(
                    apiKey,
                    secretKey,
                    authenticatedMerchant
            );

            return authenticatedMerchant;

        } finally {
            // 5. Always release our lock
            authenticationLockService.releaseLock(
                    lockKey,
                    lockValue
            );
        }
    }
}
