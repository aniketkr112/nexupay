package com.nexupay.payment.security.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
public class RedisRateLimitService {

    private static final String RATE_LIMIT_KEY_PREFIX="nexupay:rate-limit:merchant:";

    private final RateLimitProperties rateLimitProperties;
    private final RedisTemplate<String, String> redisTemplate;

    private final DefaultRedisScript<Long> rateLimitScript;

    public RedisRateLimitService(
            RedisTemplate<String, String> redisTemplate,
            RateLimitProperties rateLimitProperties) {

        this.redisTemplate = redisTemplate;
        this.rateLimitProperties = rateLimitProperties;

        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setLocation(
                new ClassPathResource("scripts/rate_limit.lua")
        );
        this.rateLimitScript.setResultType(Long.class);
    }

    public boolean tryAcquire(Long merchantId) {

        String key = RATE_LIMIT_KEY_PREFIX + merchantId;

        long currentTime = Instant.now().toEpochMilli();

        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(rateLimitProperties.getCapacity()),
                String.valueOf(rateLimitProperties.getRefillRate()),
                String.valueOf(currentTime)
        );

        return result != null && result == 1;
    }
}