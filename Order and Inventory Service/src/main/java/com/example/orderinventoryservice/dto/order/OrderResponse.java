
package com.example.orderinventoryservice.dto.order;

import com.example.orderinventoryservice.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long userId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        String shippingAddress,
        String emailAddress,
        List<OrderItemResponse> items
) {
}
