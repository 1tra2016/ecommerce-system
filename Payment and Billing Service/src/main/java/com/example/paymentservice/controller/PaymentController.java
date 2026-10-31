package com.example.paymentservice.controller;

import com.example.paymentservice.dto.api.ApiResponse;
import com.example.paymentservice.dto.payment.CreatePaymentRequest;
import com.example.paymentservice.dto.payment.CreatePaymentResponse;
import com.example.paymentservice.dto.payment.PaymentResponse;
import com.example.paymentservice.dto.attempt.AttemptResponse;
import com.example.paymentservice.mapper.AttemptMapper;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.service.IAttemptService;
import com.example.paymentservice.service.IPaymentCheckoutService;
import com.example.paymentservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;
    private final IAttemptService attemptService;
    private final IPaymentCheckoutService paymentCheckoutService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPayments(Pageable pageable) {
        Page<PaymentResponse> response = paymentService.getPayments(pageable).map(PaymentMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(
                200,response,"Lấy danh sách payments thành công"
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{paymentId}/attempts")
    public ResponseEntity<ApiResponse<List<AttemptResponse>>> getPaymentAttempts(
            @PathVariable Long paymentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String Role
    ) {
        List<AttemptResponse> response = attemptService.getAttempts(paymentId, userId, Role)
                .stream()
                .map(AttemptMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                200,response,"Lấy danh sách attemps thành công"
        ));
    }

//    @PreAuthorize("hasRole('USER')")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> checkout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreatePaymentRequest request
    ) {
        CreatePaymentResponse response =
                paymentCheckoutService.checkout(
                        request.orderNumber(),
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        201,
                        response,
                        "Tạo phiên thanh toán thành công"
                )
        );
    }
}
