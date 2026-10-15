package com.nexupay.payment.outbox.service;

import com.nexupay.payment.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxClaimService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public boolean claim(Long eventId) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime claimUntil = now.plusSeconds(30);

        int updatedRows = outboxEventRepository.claimEvent(
                eventId,
                claimUntil
        );
        log.info(
                "OUTBOX CLAIM: eventId={}, updatedRows={}, claimUntil={}",
                eventId,
                updatedRows,
                claimUntil
        );
        return updatedRows==1;

    }
}