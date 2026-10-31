package com.example.orderinventoryservice.dto.eventmessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentEvent(
        String eventId,
        String eventType,
        Long paymentId,
        Long orderId,
        Long userId,
        Long attemptId,
        String provider,
        String providerTransactionId,
        BigDecimal amount,
        String currency,
        String paymentStatus,
        LocalDateTime occurredAt,
        String reason
) {
    public static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    public static final String PAYMENT_REQUIRES_REVIEW = "PaymentRequiresReview";
}