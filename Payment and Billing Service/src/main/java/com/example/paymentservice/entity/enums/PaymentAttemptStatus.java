package com.example.paymentservice.entity.enums;

public enum PaymentAttemptStatus {
    SUPERSEDED,
    SUCCEEDED_LATE,
    EXPIRED,
    CREATED,       // Đã tạo bản ghi, chưa gửi provider
    PENDING,       // Provider đang xử lý hoặc chờ callback
    SUCCEEDED,     // Đã xác minh thanh toán thành công
    FAILED,        // Lần thử thất bại cuối cùng
    CANCELLED,     // Provider xác nhận đã hủy
    UNKNOWN
}
