package com.example.paymentservice.service.Impl;

import com.example.paymentservice.dto.payment.CreatePaymentResponse;
import com.example.paymentservice.dto.payment.PaymentGatewayResult;
import com.example.paymentservice.service.IPaymentCheckoutService;
import com.example.paymentservice.service.IPaymentLinkService;
import org.springframework.stereotype.Service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.enums.PaymentAttemptStatus;
import com.example.paymentservice.entity.enums.PaymentProvider;
import com.example.paymentservice.entity.enums.PaymentStatus;
import com.example.paymentservice.service.IAttemptService;
import com.example.paymentservice.service.IPaymentService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCheckoutServiceImpl implements IPaymentCheckoutService {

    private final IPaymentService paymentService;
    private final IAttemptService attemptService;
    private final IPaymentLinkService paymentLinkService;

    @Override
    public CreatePaymentResponse checkout(
            String orderNumber,
            Long userId
    ) {
        // 1. Validate request
        if (orderNumber == null || userId == null) {
            throw new IllegalArgumentException(
                    "Payment ID and user ID must not be null"
            );
        }

        Payment payment = paymentService.getPaymentByOrderNumber(orderNumber);

        // 3. Chỉ cho phép checkout khi Payment chưa kết thúc
        if (payment.getStatus() != PaymentStatus.PENDING
                && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Payment cannot be checked out in its current status"
            );
        }

        // 4. Tạo PaymentAttempt với idempotency key do server sinh.
        PaymentAttempt attempt = attemptService.createRetryAttempt(
                payment.getId(),
                userId,
                PaymentProvider.STRIPE,
                UUID.randomUUID().toString()
        );

        // 5. Nếu attempt đã hoàn tất thành công,
        // không tạo thêm Checkout Session.
        if (attempt.getStatus() == PaymentAttemptStatus.SUCCEEDED) {
            throw new IllegalStateException(
                    "This payment attempt has already succeeded"
            );
        }

        // 6. Gateway dùng idempotency key của attempt để tránh tạo
        // nhiều Stripe session cho cùng một attempt.
        PaymentGatewayResult gatewayResult = paymentLinkService.generatePaymentUrl(payment, attempt);

        // 7. Lưu Stripe Session ID và cập nhật trạng thái.
        PaymentAttempt updatedAttempt = attemptService.saveProviderSession(
                attempt.getId(),
                gatewayResult.providerTransactionId(),
                gatewayResult.providerStatus()
        );

        log.info(
                "Checkout session created: paymentId={}, attemptId={}, provider={}",
                payment.getId(),
                updatedAttempt.getId(),
                updatedAttempt.getProvider()
        );

        // 8. Trả URL thanh toán cho frontend
        String paymentUrl = updatedAttempt.getStatus() == PaymentAttemptStatus.PENDING
                ? gatewayResult.paymentUrl()
                : null;

        return new CreatePaymentResponse(
                payment.getId(),
                updatedAttempt.getId(),
                paymentUrl,
                updatedAttempt.getPayment().getStatus(),
                updatedAttempt.getStatus()
        );
    }
}
