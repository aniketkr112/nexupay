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
        String cacheKey = buildCacheKey(apiKey, secretKey);

        String cachedValue = redisTemplate.opsForValue().get(cacheKey);

        if (cachedValue == null) {
            //log.info("Authentication cache MISS for apiKey={}", apiKey);
            return null;
        }

        //log.info("Authentication cache HIT for apiKey={}", apiKey);

        try {
            return objectMapper.readValue(
                    cachedValue,
                    AuthenticatedMerchant.class
            );
        } catch (JsonProcessingException exception) {
            redisTemplate.delete(cacheKey);

            return null;
        }
    }

    @Override
    public void put(
            String apiKey,
            String secretKey,
            AuthenticatedMerchant authenticatedMerchant) {

        String cacheKey = buildCacheKey(apiKey, secretKey);

        try {
            String value = objectMapper.writeValueAsString(
                    authenticatedMerchant
            );

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
        // Will be implemented when credential invalidation is introduced.
    }

    private String buildCacheKey(String apiKey, String secretKey) {
        return CACHE_KEY_PREFIX
                + apiKey
                + ":"
                + secretFingerprintService.generate(secretKey);
    }
}