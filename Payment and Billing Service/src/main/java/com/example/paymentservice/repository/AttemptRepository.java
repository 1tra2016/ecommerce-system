package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.enums.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptRepository
        extends JpaRepository<PaymentAttempt, Long> {

    // Lấy lịch sử attempt của một Payment, mới nhất trước
    List<PaymentAttempt> findByPayment_IdOrderByCreatedAtDescIdDesc(
            Long paymentId
    );

    // Tìm attempt và xác nhận attempt thuộc Payment tương ứng
    Optional<PaymentAttempt> findByIdAndPayment_Id(
            Long attemptId,
            Long paymentId
    );

    // Idempotency khi tạo attempt
    Optional<PaymentAttempt> findByIdempotencyKey(
            String idempotencyKey
    );

    // Kiểm tra Payment có attempt đang ở các trạng thái được chỉ định
    boolean existsByPayment_IdAndStatusIn(
            Long paymentId,
            List<PaymentAttemptStatus> statuses
    );

    // Lấy attempt mới nhất, bất kể trạng thái
    Optional<PaymentAttempt> findFirstByPayment_IdOrderByCreatedAtDescIdDesc(
            Long paymentId
    );

    // Lấy các attempt có trạng thái cần kiểm tra
    List<PaymentAttempt> findByPayment_IdAndStatusIn(
            Long paymentId,
            List<PaymentAttemptStatus> statuses
    );

    Optional<PaymentAttempt> findByRequestReference(String requestReference);
    Optional<PaymentAttempt> findByProviderTransactionId(String providerTransactionId);
}
