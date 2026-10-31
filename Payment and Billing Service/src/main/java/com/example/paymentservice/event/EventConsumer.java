package com.example.paymentservice.event;


import com.example.paymentservice.dto.eventmessage.OrderCreatedEvent;
import com.example.paymentservice.repository.InboxEventRepository;
import com.example.paymentservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final IPaymentService paymentService;
    private final InboxEventRepository inboxEventRepository;

    @RabbitListener(
            queues = "payment.order-created.queue",
            messageConverter = "jacksonMessageConverter"
    )
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()
                || event.orderId() == null) {
            throw new IllegalArgumentException("Invalid OrderCreatedEvent");
        }

        log.info(
                "Received OrderCreatedEvent: eventId={}, orderId={}",
                event.eventId(),
                event.orderId()
        );

        int claimed = inboxEventRepository.claimEvent(
                event.eventId(),
                OrderCreatedEvent.class.getSimpleName(),
                event.orderId()
        );
        if (claimed == 0) {
            log.info("Ignoring duplicate OrderCreatedEvent: eventId={}", event.eventId());
            return;
        }

        paymentService.createPaymentFromOrderCreated(event);
    }
}
