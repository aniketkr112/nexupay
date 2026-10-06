package com.nexupay.payment.security.bloom;

import io.lettuce.core.RedisBusyException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiCredentialStreamConfig {

    private static final String STREAM_KEY =
            "nexupay:credential-events";

    private final RedisTemplate<String, String> redisTemplate;

    public void ensureConsumerGroup(String consumerGroup) {

        try {

            redisTemplate.opsForStream().createGroup(
                    STREAM_KEY,
                    ReadOffset.from("0-0"),
                    consumerGroup
            );

        } catch (Exception exception) {

            Throwable cause = exception;

            while (cause != null) {

                if (cause instanceof RedisBusyException) {
                    return;
                }

                cause = cause.getCause();
            }

            throw exception;
        }
    }
}