package com.example.paymentservice.repository;

import com.example.paymentservice.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InboxEventRepository extends JpaRepository<InboxEvent, String> {

    @Modifying
    @Query(value = """
            INSERT INTO inbox_events (event_id, event_type, aggregate_id, processed_at)
            VALUES (:eventId, :eventType, :aggregateId, CURRENT_TIMESTAMP)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int claimEvent(
            @Param("eventId") String eventId,
            @Param("eventType") String eventType,
            @Param("aggregateId") Long aggregateId
    );
}
