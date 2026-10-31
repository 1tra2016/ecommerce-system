package com.example.orderinventoryservice.mapper;

import com.example.orderinventoryservice.dto.order.OrderItemResponse;
import com.example.orderinventoryservice.dto.order.OrderResponse;
import com.example.orderinventoryservice.dto.order.OrderResponseToPay;
import com.example.orderinventoryservice.entity.Order;
import com.example.orderinventoryservice.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getShippingAddress(),
                order.getEmailAddress(),
                order.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
