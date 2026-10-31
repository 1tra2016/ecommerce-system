package com.example.paymentservice.dto.eventmessage;

public record OutboxEventPayload(
        String eventId,
        String eventType,
        Long orderId,
        Long userId
) {
    public static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    public static final String PAYMENT_REQUIRES_REVIEW = "PaymentRequiresReview";
}
