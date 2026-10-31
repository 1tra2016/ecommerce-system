package com.example.paymentservice.mapper;

import com.example.paymentservice.dto.attempt.AttemptResponse;
import com.example.paymentservice.entity.PaymentAttempt;

public class AttemptMapper {
    public static AttemptResponse toResponse(PaymentAttempt attempt) {
        return new AttemptResponse(
                attempt.getId(),
                attempt.getPayment().getId(),
                attempt.getProvider(),
                attempt.getProviderTransactionId(),
                attempt.getRequestReference(),
                attempt.getProviderStatus(),
                attempt.getStatus(),
                attempt.getCreatedAt()
        );
    }
}
