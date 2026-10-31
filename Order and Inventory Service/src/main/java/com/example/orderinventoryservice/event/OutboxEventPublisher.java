package com.example.orderinventoryservice.event;

import com.example.orderinventoryservice.dto.eventmessage.OrderCreatedEvent;
import com.example.orderinventoryservice.dto.eventmessage.OrderUpdateStatusEvent;
import com.example.orderinventoryservice.entity.OutboxEvent;
import com.example.orderinventoryservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private static final String ORDER_CREATED_EVENT =
            OrderCreatedEvent.class.getSimpleName();
    private static final String ORDER_STATUS_UPDATED_EVENT =
            OrderUpdateStatusEvent.class.getSimpleName();

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.messaging.order-exchange}")
    private String orderExchange;

    @Value("${app.messaging.order-created-routing-key}")
    private String orderCreatedRoutingKey;

    @Value("${app.messaging.order-update-status-queue}")
    private String orderUpdateStatusQueue;

    @Value("${app.outbox.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs;

    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:1000}")
    public void publishPendingEvents() {
        for (OutboxEvent event : outboxEventRepository.findReadyToPublish(
                LocalDateTime.now(), PageRequest.of(0, 100))) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {
        try {
            Object payload = payloadFor(event);
            CorrelationData correlationData = new CorrelationData(eventId(payload));

            rabbitTemplate.convertAndSend(
                    exchangeFor(event.getEventType()),
                    routingKeyFor(event.getEventType()),
                    payload,
                    message -> {
                        message.getMessageProperties().setMessageId(eventId(payload));
                        message.getMessageProperties().setDeliveryMode(
                                MessageDeliveryMode.PERSISTENT);
                        return message;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
            if (!confirm.isAck() || correlationData.getReturned() != null) {
                throw new IllegalStateException(
                        "RabbitMQ did not route outbox event " + event.getId());
            }

            event.setAttempts(event.getAttempts() + 1);
            event.setPublishedAt(LocalDateTime.now());
            event.setNextAttemptAt(null);
            outboxEventRepository.save(event);
            log.info(
                    "Published outbox event: eventId={}, type={}, aggregateId={}",
                    eventId(payload),
                    event.getEventType(),
                    event.getAggregateId()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            recordPublishFailure(event, ex);
        } catch (Exception ex) {
            recordPublishFailure(event, ex);
        }
    }

    private Object payloadFor(OutboxEvent event) {
        if (ORDER_CREATED_EVENT.equals(event.getEventType())) {
            return objectMapper.convertValue(event.getPayload(), OrderCreatedEvent.class);
        }
        if (ORDER_STATUS_UPDATED_EVENT.equals(event.getEventType())) {
            return objectMapper.convertValue(
                    event.getPayload(), OrderUpdateStatusEvent.class);
        }
        throw new IllegalArgumentException(
                "Unsupported outbox event type: " + event.getEventType());
    }

    private String exchangeFor(String eventType) {
        if (ORDER_CREATED_EVENT.equals(eventType)) {
            return orderExchange;
        }
        if (ORDER_STATUS_UPDATED_EVENT.equals(eventType)) {
            return "";
        }
        throw new IllegalArgumentException(
                "Unsupported outbox event type: " + eventType);
    }

    private String routingKeyFor(String eventType) {
        if (ORDER_CREATED_EVENT.equals(eventType)) {
            return orderCreatedRoutingKey;
        }
        if (ORDER_STATUS_UPDATED_EVENT.equals(eventType)) {
            return orderUpdateStatusQueue;
        }
        throw new IllegalArgumentException(
                "Unsupported outbox event type: " + eventType);
    }

    private String eventId(Object payload) {
        if (payload instanceof OrderCreatedEvent event) {
            return event.eventId();
        }
        if (payload instanceof OrderUpdateStatusEvent event) {
            return event.eventId();
        }
        throw new IllegalArgumentException(
                "Unsupported outbox payload: " + payload.getClass().getName());
    }

    private void recordPublishFailure(OutboxEvent event, Exception exception) {
        event.setAttempts(event.getAttempts() + 1);
        long retryDelaySeconds = Math.min(
                1L << Math.min(event.getAttempts(), 8),
                300L
        );
        event.setNextAttemptAt(LocalDateTime.now().plusSeconds(retryDelaySeconds));
        outboxEventRepository.save(event);
        log.error(
                "Could not publish outbox event id={}, type={}, attempts={}; it will be retried",
                event.getId(),
                event.getEventType(),
                event.getAttempts(),
                exception
        );
    }
}
