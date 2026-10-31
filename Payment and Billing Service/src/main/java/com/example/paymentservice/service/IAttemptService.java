package com.example.paymentservice.service;

import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.enums.PaymentProvider;

import java.math.BigDecimal;
import java.util.List;

public interface IAttemptService {

    PaymentAttempt createRetryAttempt(Long paymentId, Long userId, PaymentProvider provider, String idempotencyKey);

    PaymentAttempt markPending(Long attemptId);

    PaymentAttempt markFailed(
            Long attemptId,
            Long paymentId,
            String providerSessionId,
            String providerStatus,
            String failureCode
    );

    PaymentAttempt markSucceeded(
            Long attemptId,
            Long paymentId,
            String providerSessionId,
            String providerStatus
    );

    PaymentAttempt markExpired(
            Long attemptId,
            Long paymentId,
            String providerSessionId
    );

    PaymentAttempt saveProviderSession(Long attemptId, String sessionId, String providerStatus);

    List<PaymentAttempt> getAttempts(Long paymentId, Long userId, String Role);
}
