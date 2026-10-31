package com.example.orderinventoryservice.dto.eventmessage;

import com.example.orderinventoryservice.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderUpdateStatusEvent (
    String eventId,
    Long orderId,
    String orderNumber,
    OrderStatus orderStatus,
    Long userId,
    String email,
    BigDecimal totalAmount,
    String currency,
    LocalDateTime updateAt,
    LocalDateTime createdAt
) {}
