package com.nexupay.payment.outbox.repository;

import com.nexupay.payment.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent,Long> {

    List<OutboxEvent> findTop100ByPublishedAtIsNullOrderByIdAsc();

    @Modifying
    @Query("""
    UPDATE OutboxEvent e
    SET e.publishedAt = CURRENT_TIMESTAMP
    WHERE e.id = :id
      AND e.publishedAt IS NULL
""")
    int markPublished(@Param("id") Long id);

    @Modifying
    @Query("""
    UPDATE OutboxEvent e
    SET e.claimedAt = CURRENT_TIMESTAMP,
        e.claimUntil = :claimUntil
    WHERE e.id = :id
      AND e.publishedAt IS NULL
      AND (
          e.claimUntil IS NULL
          OR e.claimUntil < CURRENT_TIMESTAMP
      )
""")
    int claimEvent(
            @Param("id") Long id,
            @Param("claimUntil") LocalDateTime claimUntil
    );

}
