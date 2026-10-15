package com.nexupay.payment.outbox.service;

import com.nexupay.payment.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OutboxPublishService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public boolean markPublished(Long eventId) {

        return outboxEventRepository.markPublished(
                eventId
        ) == 1;
    }
}