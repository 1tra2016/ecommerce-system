package com.example.orderinventoryservice.dto.order;

import com.example.orderinventoryservice.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponseToPay(
        Long id,
        String orderNumber,
        Long userId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime paymentDeadline,
        String shippingAddress
) {
}