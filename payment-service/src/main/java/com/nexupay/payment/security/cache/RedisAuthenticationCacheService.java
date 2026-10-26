package com.nexupay.payment.security.cache;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class RedisAuthenticationCacheService implements AuthenticationCacheService {

    private static final String CACHE_KEY_PREFIX = "nexupay:auth:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SecretFingerprintService secretFingerprintService;
    private final Duration cacheTtl;

    public RedisAuthenticationCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            SecretFingerprintService secretFingerprintService,
            @Value("${nexupay.security.auth-cache-ttl}") Duration cacheTtl) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.secretFingerprintService = secretFingerprintService;
        this.cacheTtl = cacheTtl;
    }

    @Override
    public AuthenticatedMerchant get(String apiKey, String secretKey) {
        String cacheKey = buildCacheKey(apiKey);

        String cachedValue = redisTemplate.opsForValue().get(cacheKey);

        if (cachedValue == null) {
            log.info("Authentication cache MISS for apiKey={}", apiKey);
            return null;
        }

        try {
            AuthenticationCacheEntry cacheEntry =
                    objectMapper
                            .readValue(cachedValue,AuthenticationCacheEntry.class);

            String suppliedFingerprint =
                    secretFingerprintService.generate(secretKey);

            if (!suppliedFingerprint.equals(cacheEntry.secretFingerprint())) {
                log.info("Authentication cache MISS for apiKey={}", apiKey);
                return null;
            }

            log.info("Authentication cache HIT for apiKey={}", apiKey);

            return cacheEntry.authenticatedMerchant();

        } catch (JsonProcessingException exception) {
            redisTemplate.delete(cacheKey);

            log.warn(
                    "Invalid authentication cache entry removed for apiKey={}",
                    apiKey
            );

            return null;
        }
    }

    @Override
    public void put(
            String apiKey,
            String secretKey,
            AuthenticatedMerchant authenticatedMerchant) {

        String cacheKey = buildCacheKey(apiKey);

        try {

            String secretFingerPrint = secretFingerprintService.generate(secretKey);
            AuthenticationCacheEntry cacheEntry = new AuthenticationCacheEntry(secretFingerPrint,authenticatedMerchant);
            String value = objectMapper.writeValueAsString(cacheEntry);

            redisTemplate.opsForValue().set(
                    cacheKey,
                    value,
                    cacheTtl
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize authentication cache entry",
                    exception
            );
        }
    }

    @Override
    public void evict(String apiKey) {
        String cacheKey = buildCacheKey(apiKey);
        redisTemplate.delete(cacheKey);

        log.info("Authentication cache evicted for apiKey={}", apiKey);
    }

    private String buildCacheKey(String apiKey) {
        return CACHE_KEY_PREFIX+ apiKey;
    }

    private record AuthenticationCacheEntry(
            String secretFingerprint,
            AuthenticatedMerchant authenticatedMerchant) {
    }
}