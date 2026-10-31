package com.example.paymentservice.service.Impl;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.OutboxEvent;
import com.example.paymentservice.entity.enums.PaymentStatus;
import com.example.paymentservice.entity.enums.PaymentAttemptStatus;
import com.example.paymentservice.entity.enums.PaymentProvider;
import com.example.paymentservice.dto.eventmessage.OutboxEventPayload;
import com.example.paymentservice.exception.exceptions.BusinessException;
import com.example.paymentservice.exception.exceptions.DuplicateResourceException;
import com.example.paymentservice.exception.exceptions.ResourceNotFoundException;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.repository.AttemptRepository;
import com.example.paymentservice.repository.OutboxEventRepository;
import com.example.paymentservice.service.IAttemptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttemptServiceImpl implements IAttemptService {

    private final PaymentRepository paymentRepository;
    private final AttemptRepository attemptRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new attempt and marks older pending attempts as superseded.
     */
    @Override
    @Transactional
    public PaymentAttempt createRetryAttempt(
            Long paymentId,
            Long userId,
            PaymentProvider provider,
            String idempotencyKey
    ) {
        if (paymentId == null || userId == null
                || provider == null
                || idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException("Invalid payment attempt request");
        }

        var existingAttempt = attemptRepository.findByIdempotencyKey(idempotencyKey);

        if (existingAttempt.isPresent()) {
            PaymentAttempt attempt = existingAttempt.get();
            if (!attempt.getPayment().getId().equals(paymentId)
                    || !attempt.getPayment().getUserId().equals(userId)) {
                throw new DuplicateResourceException("Idempotency key has already been used");
            }
            return attempt;
        }

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));

        if (!payment.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Payment not found");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.REQUIRES_REVIEW
                || payment.getStatus() == PaymentStatus.CANCELLED
                || payment.getStatus() == PaymentStatus.EXPIRED) {
            throw new BusinessException(
                    "This payment was "+payment.getStatus()
            );
        }

        boolean hasUnresolvedAttempt =
                attemptRepository.existsByPayment_IdAndStatusIn(
                        paymentId,
                        List.of(
                                PaymentAttemptStatus.UNKNOWN
                        )
                );

        if (hasUnresolvedAttempt) {
            throw new BusinessException(
                    "An existing payment attempt is still unresolved"
            );
        }

        List<PaymentAttempt> pendingAttempts =
                attemptRepository.findByPayment_IdAndStatusIn(
                        paymentId,
                        List.of(PaymentAttemptStatus.PENDING)
                );
        pendingAttempts.forEach(attempt ->
                attempt.setStatus(PaymentAttemptStatus.SUPERSEDED));

        PaymentAttempt attempt = PaymentAttempt.builder()
                .payment(payment)
                .requestReference(UUID.randomUUID().toString().replace("-", ""))
                .provider(provider)
                .status(PaymentAttemptStatus.CREATED)
                .idempotencyKey(idempotencyKey)
                .build();

        return attemptRepository.save(attempt);
    }
    /**
     * Đánh dấu Attempt đang chềEkết quả từ provider.
     * ChềEgọi sau khi đã ghi nhận được kết quả khởi tạo giao dịch.
     */
    @Override
    @Transactional
    public PaymentAttempt markPending(Long attemptId) {
        PaymentAttempt attempt = getAttempt(attemptId);

        if (attempt.getStatus() != PaymentAttemptStatus.CREATED) {
            throw new BusinessException(
                    "Only CREATED attempts can be marked PENDING"
            );
        }

        attempt.setStatus(PaymentAttemptStatus.PENDING);
        attempt.getPayment().setStatus(PaymentStatus.PROCESSING);

        return attempt;
    }

    /**
     * Ghi nhận thất bại cuối cùng của một Attempt.
     * Không tự hủy Payment, vì khách có thềEthử lại.
     */
    @Override
    @Transactional
    public PaymentAttempt markFailed(
            Long attemptId,
            Long paymentId,
            String providerSessionId,
            String providerStatus,
            String failureCode
    ) {
        PaymentAttempt attempt = getAttempt(attemptId);
        Payment payment = lockAndValidateWebhookPayment(
                attempt, paymentId, providerSessionId);

        if (attempt.getStatus() == PaymentAttemptStatus.FAILED) {
            return attempt;
        }

        if (attempt.getStatus() == PaymentAttemptStatus.SUCCEEDED
                || attempt.getStatus() == PaymentAttemptStatus.SUCCEEDED_LATE
                || attempt.getStatus() == PaymentAttemptStatus.CANCELLED
                || attempt.getStatus() == PaymentAttemptStatus.EXPIRED) {
            return attempt;
        }

        attempt.setStatus(PaymentAttemptStatus.FAILED);
        attempt.setProviderStatus(providerStatus);
        attempt.setFailureCode(failureCode);

        // Payment vẫn mềEđềEkhách có thềEtạo Attempt mới.
        if (payment.getStatus() != PaymentStatus.SUCCESS
                && payment.getStatus() != PaymentStatus.REQUIRES_REVIEW) {
            boolean anotherAttemptIsActive =
                    attemptRepository.existsByPayment_IdAndStatusIn(
                            payment.getId(),
                            List.of(
                                    PaymentAttemptStatus.CREATED,
                                    PaymentAttemptStatus.PENDING,
                                    PaymentAttemptStatus.UNKNOWN
                            )
                    );
            payment.setStatus(anotherAttemptIsActive
                    ? PaymentStatus.PROCESSING
                    : PaymentStatus.PENDING);
        }

        return attempt;
    }

    /**
     * ChềEgọi sau khi đã xác minh giao dịch thành công
     * từ webhook hợp lềEhoặc API truy vấn provider đáng tin cậy.
     */
    @Override
    @Transactional
    public PaymentAttempt markSucceeded(
            Long attemptId,
            Long paymentId,
            String providerSessionId,
            String providerStatus
    ) {
        PaymentAttempt attempt = getAttempt(attemptId);
        Payment payment = lockAndValidateWebhookPayment(
                attempt, paymentId, providerSessionId);

        if (attempt.getStatus() == PaymentAttemptStatus.SUCCEEDED
                || attempt.getStatus() == PaymentAttemptStatus.SUCCEEDED_LATE) {
            return attempt;
        }

        boolean lateOrDuplicate = attempt.getStatus() != PaymentAttemptStatus.PENDING;

        attempt.setStatus(lateOrDuplicate
                ? PaymentAttemptStatus.SUCCEEDED_LATE
                : PaymentAttemptStatus.SUCCEEDED);
        attempt.setProviderStatus(providerStatus);

        if (lateOrDuplicate) {
            payment.setStatus(PaymentStatus.REQUIRES_REVIEW);
            savePaymentEvent(
                    OutboxEventPayload.PAYMENT_REQUIRES_REVIEW,
                    payment,
                    attempt,
                    "LATE_OR_DUPLICATE_PROVIDER_SUCCESS"
            );
        } else {
            payment.setStatus(PaymentStatus.SUCCESS);
            savePaymentEvent(
                    OutboxEventPayload.PAYMENT_SUCCEEDED,
                    payment,
                    attempt,
                    null
            );
        }

        attemptRepository.findByPayment_IdAndStatusIn(
                payment.getId(), List.of(PaymentAttemptStatus.PENDING)
        ).forEach(otherAttempt ->
                otherAttempt.setStatus(PaymentAttemptStatus.SUPERSEDED));

        return attempt;
    }

    @Override
    @Transactional
    public PaymentAttempt markExpired(
            Long attemptId,
            Long paymentId,
            String providerSessionId
    ) {
        PaymentAttempt attempt = getAttempt(attemptId);
        Payment payment = lockAndValidateWebhookPayment(
                attempt, paymentId, providerSessionId);

        if (attempt.getStatus() == PaymentAttemptStatus.SUCCEEDED
                || attempt.getStatus() == PaymentAttemptStatus.SUCCEEDED_LATE
                || attempt.getStatus() == PaymentAttemptStatus.FAILED
                || attempt.getStatus() == PaymentAttemptStatus.CANCELLED
                || attempt.getStatus() == PaymentAttemptStatus.EXPIRED) {
            return attempt;
        }

        attempt.setStatus(PaymentAttemptStatus.EXPIRED);
        attempt.setProviderStatus("expired");

        if (payment.getStatus() != PaymentStatus.SUCCESS
                && payment.getStatus() != PaymentStatus.REQUIRES_REVIEW) {
            boolean anotherAttemptIsActive =
                    attemptRepository.existsByPayment_IdAndStatusIn(
                            payment.getId(),
                            List.of(
                                    PaymentAttemptStatus.CREATED,
                                    PaymentAttemptStatus.PENDING,
                                    PaymentAttemptStatus.UNKNOWN
                            )
                    );
            payment.setStatus(anotherAttemptIsActive
                    ? PaymentStatus.PROCESSING
                    : PaymentStatus.PENDING);
        }
        return attempt;
    }

    @Override
    @Transactional
    public PaymentAttempt saveProviderSession(
            Long attemptId,
            String sessionId,
            String providerStatus
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException("Stripe session ID is required");
        }

        PaymentAttempt attempt = getAttempt(attemptId);

        if (attempt.getProvider() != PaymentProvider.STRIPE) {
            throw new BusinessException(
                    "This operation only supports Stripe"
            );
        }

        // Cho phép gọi lại cùng kết quả mà không ghi đè dữ liệu.
        if (attempt.getProviderTransactionId() != null) {
            if (attempt.getProviderTransactionId().equals(sessionId)) {
                return attempt;
            }

            throw new BusinessException(
                    "A different provider session is already associated"
            );
        }

        if (attempt.getStatus() != PaymentAttemptStatus.CREATED) {
            throw new BusinessException(
                    "Only CREATED attempts can receive a provider session"
            );
        }

        attempt.setProviderTransactionId(sessionId);
        attempt.setProviderStatus(providerStatus);
        Payment payment = attempt.getPayment();
        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.REQUIRES_REVIEW) {
            attempt.setStatus(PaymentAttemptStatus.SUPERSEDED);
        } else {
            attempt.setStatus(PaymentAttemptStatus.PENDING);
            payment.setStatus(PaymentStatus.PROCESSING);
        }

        return attempt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAttempt> getAttempts(
            Long paymentId,
            Long userId,
            String Role
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));

        if(!Role.equals("ADMIN")){
            if (!payment.getUserId().equals(userId) ) {
                throw new ResourceNotFoundException("Payment not found");
            }
        }
        return attemptRepository
                .findByPayment_IdOrderByCreatedAtDescIdDesc(paymentId);
    }

    private PaymentAttempt getAttempt(Long attemptId) {
        return attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found"));
    }

    private Payment lockAndValidateWebhookPayment(
            PaymentAttempt attempt,
            Long paymentId,
            String providerSessionId
    ) {
        if (paymentId == null || providerSessionId == null
                || providerSessionId.isBlank()) {
            throw new BusinessException("Invalid Stripe session correlation data");
        }

        Payment payment = paymentRepository.findByIdForUpdate(
                        attempt.getPayment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getId().equals(paymentId)
                || !attempt.getPayment().getId().equals(paymentId)) {
            throw new BusinessException("Stripe session does not match payment");
        }

        if (attempt.getProviderTransactionId() == null
                || !attempt.getProviderTransactionId().equals(providerSessionId)) {
            throw new BusinessException("Stripe session does not match attempt");
        }
        return payment;
    }

    private void savePaymentEvent(
            String eventType,
            Payment payment,
            PaymentAttempt attempt,
            String reason
    ) {
        OutboxEventPayload payload = new OutboxEventPayload(
                UUID.randomUUID().toString(),
                eventType,
                payment.getOrderId(),
                payment.getUserId()
        );

        outboxEventRepository.save(OutboxEvent.builder()
                .aggregateId(payment.getId())
                .eventType(eventType)
                .payload(objectMapper.valueToTree(payload))
                .build());
    }

}
