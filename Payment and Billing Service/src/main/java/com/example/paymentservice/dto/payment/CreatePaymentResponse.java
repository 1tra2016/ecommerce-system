package com.example.paymentservice.dto.payment;

import com.example.paymentservice.entity.enums.PaymentAttemptStatus;
import com.example.paymentservice.entity.enums.PaymentStatus;

public record CreatePaymentResponse(
        Long paymentId,
        Long attemptId,
        String paymentUrl,
        PaymentStatus paymentStatus,
        PaymentAttemptStatus attemptStatus
) {
}
