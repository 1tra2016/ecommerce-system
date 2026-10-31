package com.example.notificationservice.event;

import com.example.notificationservice.dto.event.OrderCreatedEvent;
import com.example.notificationservice.dto.event.OrderUpdateStatusEvent;
import com.example.notificationservice.dto.event.OtpEvent;
import com.example.notificationservice.dto.event.OutboxEventPayload;
import com.example.notificationservice.repository.InboxEventRepository;
import com.example.notificationservice.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class EventConsumer {

    private final NotificationService notificationService;
    private final InboxEventRepository inboxEventRepository;

    public EventConsumer(
            NotificationService notificationService,
            InboxEventRepository inboxEventRepository
    ) {
        this.notificationService = notificationService;
        this.inboxEventRepository = inboxEventRepository;
    }

    @RabbitListener(queues = "${app.messaging.otp-queue}")
    public void consumeOtp(OtpEvent event)
            throws Exception {
        notificationService.handleOtp(event);
    }

    @RabbitListener(
            queues = "notification.order-created.queue",
            messageConverter = "jacksonMessageConverter"
    )
    @Transactional
    public void consumeOrderCreated(OrderCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Invalid notification event");
        }
        validateEvent(
                event.eventId(),
                OrderCreatedEvent.class.getSimpleName(),
                event.orderId());
        if (!claimEvent(
                event.eventId(),
                OrderCreatedEvent.class.getSimpleName(),
                event.orderId())) {
            return;
        }
        notificationService.handleOrderCreated(event);
    }

    @RabbitListener(queues = "${app.messaging.order-update-status-queue}")
    @Transactional
    public void consumeOrderStatusUpdate(OrderUpdateStatusEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Invalid notification event");
        }
        validateEvent(
                event.eventId(),
                OrderUpdateStatusEvent.class.getSimpleName(),
                event.orderId());
        if (!claimEvent(
                event.eventId(),
                OrderUpdateStatusEvent.class.getSimpleName(),
                event.orderId())) {
            return;
        }
        notificationService.handleOrderUpdateStatus(event);
    }

    @RabbitListener(queues = "${app.messaging.payment-review-queue}")
    @Transactional
    public void consumePaymentReview(OutboxEventPayload event) {
        if (event == null) {
            throw new IllegalArgumentException("Invalid notification event");
        }
        validateEvent(
                event.eventId(),
                event.eventType(),
                event.orderId());
        if (!claimEvent(event.eventId(), event.eventType(), event.orderId())) {
            return;
        }
        notificationService.handlePaymentReview(event);
    }

    private boolean claimEvent(String eventId, String eventType, Long aggregateId) {
        int claimed = inboxEventRepository.claimEvent(eventId, eventType, aggregateId);
        if (claimed == 0) {
            log.info("Ignoring duplicate notification event: eventId={}", eventId);
            return false;
        }
        return true;
    }

    private void validateEvent(String eventId, String eventType, Long aggregateId) {
        if (eventId == null || eventId.isBlank()
                || eventType == null || eventType.isBlank()
                || aggregateId == null) {
            throw new IllegalArgumentException("Invalid notification event");
        }
    }
}