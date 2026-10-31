package com.example.orderinventoryservice.controller;

import com.example.orderinventoryservice.dto.api.ApiResponse;
import com.example.orderinventoryservice.dto.order.CreateOrderRequest;
import com.example.orderinventoryservice.dto.order.OrderResponse;
import com.example.orderinventoryservice.dto.order.OrderResponseToPay;
import com.example.orderinventoryservice.entity.enums.OrderStatus;
import com.example.orderinventoryservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(HttpStatus.CREATED.value(), order, "Order created and stock reserved"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(Pageable pageable) {
        Page<OrderResponse> response = orderService.getOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(200, response));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(
            @PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(200, order));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                orderService.updateOrderStatus(orderId, status),
                "Đã cập nhật trạng thái đơn hàng"));
    }

    // @PreAuthorize("hasRole('USRER')")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                orderService.cancelOrder(orderId, userId),
                "Đã hủy đơn hàng"));
    }
}
