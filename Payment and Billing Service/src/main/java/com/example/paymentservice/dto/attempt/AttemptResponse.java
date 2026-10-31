package com.example.paymentservice.dto.attempt;

import com.example.paymentservice.entity.enums.PaymentAttemptStatus;
import com.example.paymentservice.entity.enums.PaymentProvider;

import java.time.LocalDateTime;

public record AttemptResponse(
        Long id,
        Long paymentId,
        PaymentProvider provider,
        String providerTransactionId,
        String requestReference,
        String providerStatus,
        PaymentAttemptStatus attemptStatus,
        LocalDateTime createdAt
) {
}