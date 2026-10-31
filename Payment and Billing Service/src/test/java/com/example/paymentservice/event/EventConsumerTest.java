package com.example.paymentservice.event;

import com.example.paymentservice.dto.eventmessage.OrderCreatedEvent;
import com.example.paymentservice.repository.InboxEventRepository;
import com.example.paymentservice.service.IPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventConsumerTest {

    private IPaymentService paymentService;
    private InboxEventRepository inboxEventRepository;
    private EventConsumer listener;

    @BeforeEach
    void setUp() {
        paymentService = mock(IPaymentService.class);
        inboxEventRepository = mock(InboxEventRepository.class);
        listener = new EventConsumer(paymentService, inboxEventRepository);
    }

    @Test
    void processesOrderCreatedEventWhenFirstSeen() {
        OrderCreatedEvent event = orderCreatedEvent();
        when(inboxEventRepository.claimEvent(
                "event-1", "OrderCreatedEvent", 42L)).thenReturn(1);

        listener.handleOrderCreated(event);

        verify(paymentService).createPaymentFromOrderCreated(event);
    }

    @Test
    void skipsOrderCreatedEventWhenAlreadyProcessed() {
        OrderCreatedEvent event = orderCreatedEvent();
        when(inboxEventRepository.claimEvent(
                "event-1", "OrderCreatedEvent", 42L)).thenReturn(0);

        listener.handleOrderCreated(event);

        verify(paymentService, never()).createPaymentFromOrderCreated(event);
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(
                "event-1",
                42L,
                "ORD-42",
                7L,
                null,
                "VND",
                null,
                LocalDateTime.of(2026, 10, 31, 1, 0)
        );
    }
}
