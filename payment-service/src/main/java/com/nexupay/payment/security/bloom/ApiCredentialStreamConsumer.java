package com.nexupay.payment.security.bloom;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiCredentialStreamConsumer {

    private static final String STREAM_KEY =
            "nexupay:credential-events";

    private final RedisTemplate<String, String> redisTemplate;
    private final ApiKeyBloomFilterService bloomFilterService;
    private final ApiCredentialStreamConfig streamConfig;



    @Value("${nexupay.bloom.consumer-group}")
    private String consumerGroup;

    @Value("${nexupay.bloom.consumer-name}")
    private String consumerName;

    @PostConstruct
    public void initialize() {
        streamConfig.ensureConsumerGroup(consumerGroup);
    }

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        List<MapRecord<String, Object, Object>> records =
                redisTemplate.opsForStream().read(
                        Consumer.from(
                                consumerGroup,
                                consumerName
                        ),
                        StreamOffset.create(
                                STREAM_KEY,
                                ReadOffset.lastConsumed()
                        )
                );

        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {

            Map<Object, Object> value = record.getValue();

            String eventType =
                    String.valueOf(value.get("eventType"));

            if ("API_CREDENTIAL_CREATED".equals(eventType)) {

                String apiKey =
                        String.valueOf(value.get("payload"));

                bloomFilterService.add(apiKey);
            }

            redisTemplate.opsForStream()
                    .acknowledge(
                            STREAM_KEY,
                            consumerGroup,
                            record.getId()
                    );
        }
    }
}