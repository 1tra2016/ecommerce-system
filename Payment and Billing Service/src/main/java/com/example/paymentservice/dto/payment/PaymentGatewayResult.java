package com.example.paymentservice.dto.payment;

public record PaymentGatewayResult(
        String paymentUrl,
        String providerTransactionId,
        String providerStatus
) {
}