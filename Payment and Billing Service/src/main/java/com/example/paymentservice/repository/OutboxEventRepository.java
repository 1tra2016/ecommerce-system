package com.example.paymentservice.repository;

import com.example.paymentservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            SELECT event
            FROM OutboxEvent event
            WHERE event.publishedAt IS NULL
              AND (event.nextAttemptAt IS NULL OR event.nextAttemptAt <= :now)
            ORDER BY event.createdAt ASC
            """)
    List<OutboxEvent> findReadyToPublish(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    boolean existsByAggregateIdAndEventType(Long aggregateId, String eventType
    );
}
