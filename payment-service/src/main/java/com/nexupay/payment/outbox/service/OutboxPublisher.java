package com.nexupay.payment.outbox.service;

import com.nexupay.payment.outbox.entity.OutboxEvent;
import com.nexupay.payment.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final String STREAM_KEY = "nexupay:credential-events";

    private final OutboxEventRepository outboxEventRepository;
    private final RedisTemplate<String,String> redisTemplate;


    @Scheduled(fixedDelay = 10000)
    public void publishEvents(){

        List<OutboxEvent> events =
                outboxEventRepository.
                        findTop100ByPublishedAtIsNullOrderByIdAsc();

        for(OutboxEvent event:events){

            try {
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
                event.markPublished();
                outboxEventRepository.save(event);
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
