package com.example.orderinventoryservice.dto.eventmessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        String eventId,
        Long orderId,
        String orderNumber,
        Long userId,
        String email,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime paymentDeadline,
        LocalDateTime createdAt
) {}