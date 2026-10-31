package com.example.notificationservice.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderUpdateStatusEvent(
        String eventId,
        Long orderId,
        String orderNumber,
        String orderStatus,
        Long userId,
        String email,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime updateAt,
        LocalDateTime createdAt
) {
}
