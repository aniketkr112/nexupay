package com.nexupay.payment.credential.service;

import com.nexupay.payment.common.cryptography.SecretKeyHasher;
import com.nexupay.payment.common.exception.AuthenticationServiceException;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import com.nexupay.payment.security.cache.AuthenticationCacheService;
import com.nexupay.payment.security.cache.AuthenticationLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CredentialAuthenticationService {

    private static final String LOCK_KEY_PREFIX= "nexupay:auth:lock:";
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

        // 1. Fast path: Redis cache
        AuthenticatedMerchant cachedMerchant =
                authenticationCacheService.get(apiKey, secretKey);

        if (cachedMerchant != null) {
            return cachedMerchant;
        }

        String lockKey = LOCK_KEY_PREFIX + apiKey;
        String lockValue = UUID.randomUUID().toString();

        // 2. Try to become the request responsible for loading the cache
        boolean lockAcquired = authenticationLockService.acquireLock(
                lockKey,
                lockValue,
                Duration.ofSeconds(5)
        );

        if (lockAcquired) {
            try {

                log.info("AUTH LOCK ACQUIRED apiKey={}", apiKey);
                // 3. Double-check Redis.
                // Another request may have populated it just before we acquired the lock.
                cachedMerchant =
                        authenticationCacheService.get(apiKey, secretKey);

                if (cachedMerchant != null) {
                    log.info("AUTH DOUBLE CHECK HIT apiKey={}", apiKey);
                    return cachedMerchant;
                }

                log.info("AUTH DB + BCRYPT START apiKey={}", apiKey);
                // 4. Only the lock owner reaches DB + BCrypt
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

                log.info("AUTH DB + BCRYPT SUCCESS apiKey={}", apiKey);
                // 5. Populate Redis
                authenticationCacheService.put(
                        apiKey,
                        secretKey,
                        authenticatedMerchant
                );
                log.info("AUTH CACHE PUT apiKey={}", apiKey);

                return authenticatedMerchant;

            } finally {

                // 6. Only the owner can release this lock
                authenticationLockService.releaseLock(
                        lockKey,
                        lockValue
                );
                log.info("AUTH LOCK RELEASED apiKey={}", apiKey);
            }
        }
        log.info("AUTH LOCK BUSY apiKey={}", apiKey);

        // 7. Another request is already loading the cache.
        // Wait briefly and check Redis again.
        for (int attempt = 0; attempt < 40; attempt++) {

            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return null;
            }

            cachedMerchant =
                    authenticationCacheService.get(apiKey, secretKey);

            if (cachedMerchant != null) {
                log.info(
                        "AUTH WAIT SUCCESS apiKey={} attempt={}",
                        apiKey,
                        attempt
                );
                return cachedMerchant;
            }
        }

        // 8. Cache still unavailable after waiting
        throw new AuthenticationServiceException(
                "Authentication service temporarily unavailable"
        );
    }
}
