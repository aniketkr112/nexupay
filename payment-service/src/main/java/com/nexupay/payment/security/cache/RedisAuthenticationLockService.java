package com.nexupay.payment.security.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RedisAuthenticationLockService implements AuthenticationLockService{


    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
                    Long.class
            );
    private final RedisTemplate<String,String> redisTemplate;


    @Override
    public boolean acquireLock(String lockKey, String lockValue, Duration ttl) {

        Boolean acquire = redisTemplate.opsForValue()
                .setIfAbsent(lockKey,lockValue,ttl);

        return Boolean.TRUE.equals(acquire);
    }

    @Override
    public void releaseLock(String lockKey, String lockValue) {
        redisTemplate.execute(
                RELEASE_LOCK_SCRIPT,
                Collections.singletonList(lockKey),
                lockValue
        );
    }
}
