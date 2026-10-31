package com.example.paymentservice.event;

import com.example.paymentservice.dto.eventmessage.OutboxEventPayload;
import com.example.paymentservice.entity.OutboxEvent;
import com.example.paymentservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private static final String DEFAULT_EXCHANGE = "";

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value(
            "${app.messaging.payment-succeeded-queue}")
    private String paymentSucceededQueue;

    @org.springframework.beans.factory.annotation.Value(
            "${app.messaging.payment-review-queue}")
    private String paymentReviewQueue;

    @org.springframework.beans.factory.annotation.Value(
            "${app.outbox.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs;

    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:1000}")
    public void publishPendingEvents() {
        for (OutboxEvent event :
                outboxEventRepository.findReadyToPublish(
                        LocalDateTime.now(), PageRequest.of(0, 100))) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {
        try {
            OutboxEventPayload payload = objectMapper.treeToValue(
                    event.getPayload(), OutboxEventPayload.class);
            String queue = destinationFor(event.getEventType());
            CorrelationData correlationData =
                    new CorrelationData(payload.eventId());

            rabbitTemplate.convertAndSend(
                    DEFAULT_EXCHANGE,
                    queue,
                    payload,
                    message -> {
                        message.getMessageProperties().setMessageId(payload.eventId());
                        message.getMessageProperties().setType(event.getEventType());
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
                    payload.eventId(),
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

    private String destinationFor(String eventType) {
        return switch (eventType) {
            case OutboxEventPayload.PAYMENT_SUCCEEDED -> paymentSucceededQueue;
            case OutboxEventPayload.PAYMENT_REQUIRES_REVIEW -> paymentReviewQueue;
            default -> throw new IllegalArgumentException(
                    "Unsupported outbox event type: " + eventType);
        };
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
