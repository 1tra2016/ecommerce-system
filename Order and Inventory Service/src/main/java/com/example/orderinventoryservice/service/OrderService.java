
package com.example.orderinventoryservice.service;

import com.example.orderinventoryservice.client.product.ProductGrpcClient;
import com.example.orderinventoryservice.client.product.ProductInfo;
import com.example.orderinventoryservice.dto.eventmessage.OrderUpdateStatusEvent;
import com.example.orderinventoryservice.dto.order.CreateOrderRequest;
import com.example.orderinventoryservice.dto.order.OrderItemRequest;
import com.example.orderinventoryservice.dto.order.OrderResponse;
import com.example.orderinventoryservice.entity.Order;
import com.example.orderinventoryservice.entity.enums.OrderStatus;
import com.example.orderinventoryservice.exception.exceptions.BusinessException;
import com.example.orderinventoryservice.exception.exceptions.ResourceNotFoundException;
import com.example.orderinventoryservice.mapper.OrderMapper;
import com.example.orderinventoryservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductGrpcClient productGrpcClient;
    private final OrderTransactionService orderTransactionService;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ReservationService reservationService;
    private final OutboxEventService outboxEventService;

    // @Transactional
    public OrderResponse createOrder(
            Long userId,
            CreateOrderRequest request) {

        if (request == null
                || request.items() == null
                || request.items().isEmpty()) {
            throw new BusinessException(
                    "Order must contain at least one item");
        }

        String addres = request.shippingAddress();
        String email = request.emailAddress();

        // Không cho phép cùng một variant xuất hiện nhiều lần.
        Set<Long> variantIds = new HashSet<>();

        for (OrderItemRequest item : request.items()) {
            if (item == null
                    || item.variantId() == null
                    || item.variantId() <= 0
                    || item.quantity() == null
                    || item.quantity() <= 0) {
                throw new BusinessException(
                        "Each item must have a valid variant ID and quantity");
            }

            if (!variantIds.add(item.variantId())) {
                throw new BusinessException(
                        "Duplicate variant ID: " + item.variantId());
            }
        }

        // Một batch gRPC call thay vì gọi riêng từng variant.
        Map<Long, ProductInfo> products = productGrpcClient.getVariants(variantIds);

        if (products == null) {
            throw new BusinessException(
                    "Unable to retrieve product information");
        }

        for (Long variantId : variantIds) {
            ProductInfo product = products.get(variantId);

            if (product == null) {
                throw new BusinessException(
                        "Product variant not found: " + variantId);
            }

            if (!product.active() || !product.sellable()) {
                throw new BusinessException(
                        "Product variant is not available: " + variantId);
            }

            if (product.productId() == null
                    || product.sku() == null
                    || product.sku().isBlank()
                    || product.productName() == null
                    || product.productName().isBlank()
                    || product.unitPrice() == null
                    || product.unitPrice().signum() < 0) {
                throw new BusinessException("Invalid product data for variant: " + variantId);
            }
        }

        Order savedOrder = orderTransactionService.createOrder(userId, addres, email, request.items(), products);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return orderMapper.toResponse(order);
    }

    @Scheduled(fixedDelayString = "${inventory.reservation-expiry-scan-ms:60000}")
    @Transactional
    public void expireOverdueReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> overdueOrders = orderRepository
                .findByStatusAndPaymentDeadlineLessThanEqualOrderByPaymentDeadlineAscIdAsc(
                        OrderStatus.PENDING, now, PageRequest.of(0, 100));

        for (Order overdueOrder : overdueOrders) {
            Order order = orderRepository.findByIdForUpdate(overdueOrder.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Order not found: " + overdueOrder.getId()));
            if (order.getStatus() != OrderStatus.PENDING
                    || order.getPaymentDeadline() == null
                    || order.getPaymentDeadline().isAfter(now)) {
                continue;
            }

            reservationService.expireOrderReservations(order.getId());
            changeOrderStatus(order, OrderStatus.CANCELLED);
        }
    }

    @Transactional
    public void handlePaymentSucceeded(Long orderId) {
        Order order = findOrderForUpdate(orderId);
        if (order.getStatus() == OrderStatus.CONFIRMED
                || order.getStatus() == OrderStatus.SHIPPING
                || order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.PAYMENT_REVIEW) {
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELLED
                || !reservationService.canConfirmOrderReservations(orderId)) {
            order.setStatus(OrderStatus.PAYMENT_REVIEW);
            return;
        }

        reservationService.confirmOrderReservations(orderId);

        changeOrderStatus(order, OrderStatus.CONFIRMED);
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {
        Order order = findOrderForUpdate(orderId);
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == newStatus) {
            return orderMapper.toResponse(order);
        }

        switch (newStatus) {
            case PAYMENT_REVIEW -> {
                OrderService.log.info("Order: {} need review", orderId);
            }
            case CONFIRMED -> {
                if (currentStatus != OrderStatus.PENDING
                        && currentStatus != OrderStatus.PAYMENT_REVIEW) {
                    throw new BusinessException("Only PENDING and PAYMENT_REVIEW orders can be confirmed");
                }
                reservationService.confirmOrderReservations(orderId);
            }

            case SHIPPING -> {
                if (currentStatus != OrderStatus.CONFIRMED) {
                    throw new BusinessException("Only CONFIRMED orders can be shipped");
                }
            }

            case COMPLETED -> {
                if (currentStatus != OrderStatus.SHIPPING) {
                    throw new BusinessException("Only SHIPPING orders can be completed");
                }
            }

            default -> throw new BusinessException("Invalid order status transition");
        }
        changeOrderStatus(order, newStatus);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(
            Long orderId,
            Long requesterId) {
        Order order = findOrderForUpdate(orderId);
        checkOwner(order, requesterId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only PENDING orders can be cancelled");
        }
        reservationService.releaseOrderReservations(orderId);
        changeOrderStatus(order, OrderStatus.CANCELLED);
        return orderMapper.toResponse(order);
    }

    private void changeOrderStatus(Order order, OrderStatus newStatus) {
        // Trạng thái k đổi
        if (order.getStatus() == newStatus) {
            return;
        }

        order.setStatus(newStatus);

        OrderUpdateStatusEvent event = new OrderUpdateStatusEvent(
                UUID.randomUUID().toString(),
                order.getId(),
                order.getOrderNumber(),
                newStatus,
                order.getUserId(),
                order.getEmailAddress(),
                order.getTotalAmount(),
                order.getCurrency(),
                LocalDateTime.now(),
                order.getCreatedAt());

        outboxEventService.enqueue(
                order.getId(),
                OrderUpdateStatusEvent.class.getSimpleName(),
                event);
    }

    private Order findOrderForUpdate(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void checkOwner(Order order, Long requesterId) {
        if (!order.getUserId().equals(requesterId)) {
            throw new ResourceNotFoundException(
                    "User have id " + requesterId + " can not update order with id: " + order.getId());
        }
    }
}
