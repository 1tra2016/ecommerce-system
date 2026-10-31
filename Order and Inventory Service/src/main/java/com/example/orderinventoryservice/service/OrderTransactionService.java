
package com.example.orderinventoryservice.service;

import com.example.orderinventoryservice.client.product.ProductInfo;
import com.example.orderinventoryservice.dto.eventmessage.OrderCreatedEvent;
import com.example.orderinventoryservice.dto.order.OrderItemRequest;
import com.example.orderinventoryservice.entity.Order;
import com.example.orderinventoryservice.entity.OrderItem;
import com.example.orderinventoryservice.entity.InventoryReservation;
import com.example.orderinventoryservice.entity.enums.OrderStatus;
import com.example.orderinventoryservice.exception.exceptions.BusinessException;
import com.example.orderinventoryservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderTransactionService {

    private final OrderRepository orderRepository;
    private final ReservationService reservationService;
    private final OutboxEventService outboxEventService;

    @Value("${app.order.payment-expiration-minutes}")
    private long paymentExpirationMinutes;

    @Transactional
    public Order createOrder(
            Long userId,
            String shippingAddress,
            String email,
            List<OrderItemRequest> requests,
            Map<Long, ProductInfo> products) {

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .paymentDeadline(LocalDateTime.now().plusMinutes(paymentExpirationMinutes))
                .shippingAddress(shippingAddress)
                .emailAddress(email)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest request : requests) {
            ProductInfo product = products.get(request.variantId());
            if (!product.active()) {
                throw new BusinessException("Product variant is inactive: " + product.variantId());
            }

            BigDecimal subtotal = product.unitPrice()
                    .multiply(BigDecimal.valueOf(request.quantity()));

            OrderItem item = OrderItem.builder()
                    .productId(product.productId())
                    .variantId(product.variantId())
                    .sku(product.sku())
                    .productName(product.productName())
                    .unitPrice(product.unitPrice())
                    .quantity(request.quantity())
                    .subtotal(subtotal)
                    .build();

            order.addItem(item);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        // Lưu order để lấy ID trước khi tạo reservation.
        // Nếu giữ hàng thất bại, transaction này sẽ rollback.
        Order savedOrder = orderRepository.save(order);

        // Giữ hàng theo thứ tự variantId để giảm nguy cơ deadlock
        // khi các đơn đồng thời chứa nhiều variant giống nhau.
        List<OrderItem> sortedItems = savedOrder.getItems()
                .stream()
                .sorted(Comparator.comparing(OrderItem::getVariantId))
                .toList();

        for (OrderItem item : sortedItems) {
            InventoryReservation reservation =
                    reservationService.reserve(
                            savedOrder.getId(),
                            item.getVariantId(),
                            item.getQuantity()
                    );

            // Không cần xử lý thêm reservation ở đây:
            // reserve() đã cập nhật inventory và lưu reservation.
        }

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserId(),
                savedOrder.getEmailAddress(),
                savedOrder.getTotalAmount(),
                savedOrder.getCurrency(),
                savedOrder.getPaymentDeadline(),
                savedOrder.getCreatedAt()
        );
        outboxEventService.enqueue(
                savedOrder.getId(),
                OrderCreatedEvent.class.getSimpleName(),
                event
        );

        return savedOrder;
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID();
    }
}
