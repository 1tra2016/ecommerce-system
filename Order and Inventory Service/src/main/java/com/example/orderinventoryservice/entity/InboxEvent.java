package com.example.orderinventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "inbox_events")
@Getter
@Setter
@NoArgsConstructor
public class InboxEvent {
    @Id
    @Column(name = "event_id", length = 100, nullable = false)
    private String eventId;
    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;
    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public InboxEvent(String eventId, String eventType, Long aggregateId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.processedAt = Instant.now();
    }
}