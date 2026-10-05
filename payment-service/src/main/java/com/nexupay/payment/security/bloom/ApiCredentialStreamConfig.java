package com.nexupay.payment.security.bloom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
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
            redisTemplate.execute((RedisCallback<Object>) (connection) -> {

                connection.execute(
                        "XGROUP",
                        STREAM_KEY.getBytes(),
                        "CREATE".getBytes(),
                        consumerGroup.getBytes(),
                        "0-0".getBytes(),
                        "MKSTREAM".getBytes()
                );

                return null;
            });

        } catch (Exception exception) {

            /*
             * Group may already exist.
             *
             * We don't want application startup
             * to fail in that case.
             */
        }
    }
}