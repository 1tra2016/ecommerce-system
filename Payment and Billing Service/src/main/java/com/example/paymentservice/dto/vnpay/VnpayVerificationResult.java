package com.example.paymentservice.dto.vnpay;

public record VnpayVerificationResult(
        boolean signatureValid,
        boolean successful,
        String txnRef,
        Long amountMinor,
        String transactionNo,
        String responseCode,
        String transactionStatus,
        String message
) {}