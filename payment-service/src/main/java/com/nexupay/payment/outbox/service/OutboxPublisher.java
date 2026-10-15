package com.nexupay.payment.outbox.service;

import com.nexupay.payment.outbox.entity.OutboxEvent;
import com.nexupay.payment.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final String STREAM_KEY = "nexupay:credential-events";

    private final OutboxEventRepository outboxEventRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final OutboxClaimService outboxClaimService;
    private final OutboxPublishService outboxPublishService;

    @Value("${nexupay.bloom.consumer-name}")
    private String consumerName;


    @Scheduled(fixedDelay = 10000)
    public void publishEvents(){

        List<OutboxEvent> events =
                outboxEventRepository.
                        findTop100ByPublishedAtIsNullOrderByIdAsc();

        for(OutboxEvent event:events){

            try {

                boolean claimed = outboxClaimService.claim(event.getId());

                if (!claimed) {
                    log.info(
                            "Outbox event {} was already claimed by another instance. Skipping.",
                            event.getId()
                    );
                    continue;
                }

                redisTemplate.opsForStream().add(
                        STREAM_KEY,
                        Map.of(
                                "eventId",event.getId().toString(),
                                "eventType",event.getEventType(),
                                "aggregateId",event.getAggregateId(),
                                "aggregateType",event.getAggregateType(),
                                "payload",event.getPayload(),
                                "createdAt",event.getCreatedAt().toString()
                        )
                );
                outboxPublishService.markPublished(event.getId());
                boolean marked = outboxPublishService.markPublished(event.getId());
                if (!marked) {
                    log.info(
                            "Outbox event {} was already marked by another instance. Skipping.",
                            event.getId()
                    );
                }
            } catch (Exception exception) {
                log.error(
                        "Failed to publish outbox event id={}",
                        event.getId(),
                        exception
                );
            }
        }
    }


}
