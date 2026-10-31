package com.example.paymentservice.repository;

import com.example.paymentservice.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    // Mỗi Order chỉ có một Payment theo thiết kế hiện tại
    Optional<Payment> findByOrderId(Long orderId);

    // Tìm Payment theo Order và chủ sở hữu
    Optional<Payment> findByOrderNumber(String orderNumber);

    // Kiểm tra quyền sở hữu khi truy vấn Payment bằng ID
    Optional<Payment> findByIdAndUserId(
            Long paymentId,
            Long userId
    );

    // Idempotency khi tạo Payment
    Optional<Payment> findByIdempotencyKey(
            String idempotencyKey
    );

    // Khóa Payment khi thực hiện nghiệp vụ cần cập nhật đồng thời
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.id = :paymentId
    """)
    Optional<Payment> findByIdForUpdate(
            @Param("paymentId") Long paymentId
    );
}
