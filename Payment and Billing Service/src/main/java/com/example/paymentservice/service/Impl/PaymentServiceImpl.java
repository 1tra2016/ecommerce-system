package com.example.paymentservice.service.Impl;

import com.example.paymentservice.dto.eventmessage.OrderCreatedEvent;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.enums.PaymentAttemptStatus;
import com.example.paymentservice.entity.enums.PaymentProvider;
import com.example.paymentservice.entity.enums.PaymentStatus;
import com.example.paymentservice.exception.exceptions.ResourceNotFoundException;
import com.example.paymentservice.repository.AttemptRepository;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.IAttemptService;
import com.example.paymentservice.service.IPaymentService;

import com.stripe.exception.SignatureVerificationException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    private static final String CURRENCY_VND = "VND";

    private static final List<PaymentAttemptStatus> UNRESOLVED_STATUSES =
            List.of(
                    PaymentAttemptStatus.CREATED,
                    PaymentAttemptStatus.PENDING,
                    PaymentAttemptStatus.SUPERSEDED,
                    PaymentAttemptStatus.UNKNOWN
            );

    private final PaymentRepository paymentRepository;
    private final AttemptRepository attemptRepository;
    private final IAttemptService attemptService;

    @Override
    @Transactional
    public void createPaymentFromOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.orderId() == null
                || event.orderNumber() == null
                || event.userId() == null
                || event.totalAmount() == null
                || event.totalAmount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "OrderCreatedEvent không hợp lệ"
            );
        }

        // Idempotency theo Order: event được gửi lại cũng không
        // tạo thêm Payment cho cùng một Order.
        var existing = paymentRepository.findByOrderId(event.orderId());

        if (existing.isPresent()) {
            log.info(
                    "Payment already exists for orderId={}",
                    event.orderId()
            );
            return;
        }

        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .userId(event.userId())
                .orderNumber(event.orderNumber())
                .amount(event.totalAmount())
                .currency(
                        event.currency() == null || event.currency().isBlank()
                                ? CURRENCY_VND
                                : event.currency()
                )
                .status(PaymentStatus.PENDING)
                .idempotencyKey("ORDER-" + event.orderId())
                .build();

        paymentRepository.save(payment);

        log.info(
                "Created Payment for orderId={}, amount={}",
                event.orderId(),
                event.totalAmount()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId, Long userId) {
        return paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Payment")
                );
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Payment getPaymentByOrder(Long orderId, Long userId) {
//        return paymentRepository.findByOrderIdAndUserId(orderId, userId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Không tìm thấy Payment của Order này")
//                );
//    }
    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderNumber(String orderNumber) {
        return paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy Payment của Order này")
                );
    }

    @Override
    @Transactional
    public void cancelPayment(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy Payment")
                );

        checkOwner(payment, userId);

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Payment đã thành công; cần thực hiện nghiệp vụ hoàn tiền"
            );
        }

        if (payment.getStatus() == PaymentStatus.REQUIRES_REVIEW) {
            throw new IllegalStateException(
                    "Payment requires review and cannot be cancelled"
            );
        }

        if (payment.getStatus() == PaymentStatus.EXPIRED) {
            return;
        }

        /*
         * Chưa có cancelAttempt/cancelProvider trong IAttemptService.
         * Vì vậy không được đánh dấu Payment CANCELLED nếu provider
         * vẫn có thể thu tiền từ một attempt đang xử lý.
         */
        if (attemptRepository.existsByPayment_IdAndStatusIn(
                paymentId, UNRESOLVED_STATUSES)) {
            throw new IllegalStateException(
                    "Có attempt chưa kết thúc; cần hủy hoặc đối soát "
                            + "với provider trước"
            );
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> getPayments(Pageable pageable) {
        // Chỉ cho ADMIN hoặc quyền nội bộ phù hợp gọi endpoint này.
        return paymentRepository.findAll(pageable);
    }

    private void checkOwner(Payment payment, Long userId) {
        if (!Objects.equals(payment.getUserId(), userId)) {
            throw new IllegalArgumentException(
                    "Bạn không có quyền truy cập Payment này"
            );
        }
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank() || key.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key không hợp lệ"
            );
        }
    }

}
