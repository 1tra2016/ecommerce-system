package com.example.orderinventoryservice.event;

import com.example.orderinventoryservice.dto.eventmessage.PaymentEvent;
import com.example.orderinventoryservice.entity.InboxEvent;
import com.example.orderinventoryservice.entity.enums.OrderStatus;
import com.example.orderinventoryservice.repository.InboxEventRepository;
import com.example.orderinventoryservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final OrderService orderService;
    private final InboxEventRepository inboxEventRepository;

    @RabbitListener(queues = {
            "${app.messaging.payment-succeeded-queue}",
            "${app.messaging.payment-review-queue}"
    }, messageConverter = "jacksonMessageConverter")
    @Transactional
    public void consume(PaymentEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()
                || event.orderId() == null) {
            throw new IllegalArgumentException("Invalid payment event");
        }
        if (inboxEventRepository.existsById(event.eventId())) {
            log.info("Ignoring duplicate payment event: eventId={}", event.eventId());
            return;
        }
        log.info("Receiving payment event have status: {}", event.eventType());

        switch (event.eventType()) {
            case PaymentEvent.PAYMENT_SUCCEEDED ->
                    orderService.handlePaymentSucceeded(event.orderId());
            case PaymentEvent.PAYMENT_REQUIRES_REVIEW ->
                    orderService.updateOrderStatus(event.orderId(), OrderStatus.PAYMENT_REVIEW);
            default -> throw new IllegalArgumentException(
                    "Unsupported payment event type: " + event.eventType());
        }

        inboxEventRepository.save(new InboxEvent(
                event.eventId(), event.eventType(), event.orderId()));
        log.info("Consumed payment event: eventId={}, type={}, orderId={}",
                event.eventId(), event.eventType(), event.orderId());
    }
}