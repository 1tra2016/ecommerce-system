package com.example.notificationservice.event;

import com.example.notificationservice.dto.event.OrderCreatedEvent;
import com.example.notificationservice.repository.InboxEventRepository;
import com.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventConsumerTest {

    private NotificationService notificationService;
    private InboxEventRepository inboxEventRepository;
    private EventConsumer consumer;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        inboxEventRepository = mock(InboxEventRepository.class);
        consumer = new EventConsumer(
                notificationService,
                inboxEventRepository
        );
    }

    @Test
    void processesOrderCreatedEventWhenFirstSeen() {
        OrderCreatedEvent event = orderCreatedEvent();
        when(inboxEventRepository.claimEvent(
                "event-1", "OrderCreatedEvent", 42L)).thenReturn(1);

        consumer.consumeOrderCreated(event);

        verify(notificationService).handleOrderCreated(event);
    }

    @Test
    void skipsOrderCreatedEventWhenAlreadyProcessed() {
        OrderCreatedEvent event = orderCreatedEvent();
        when(inboxEventRepository.claimEvent(
                "event-1", "OrderCreatedEvent", 42L)).thenReturn(0);

        consumer.consumeOrderCreated(event);

        verify(notificationService, never()).handleOrderCreated(event);
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(
                "event-1",
                42L,
                "ORD-42",
                7L,
                "customer@example.com",
                null,
                "VND",
                null,
                LocalDateTime.of(2026, 10, 31, 1, 0)
        );
    }
}
