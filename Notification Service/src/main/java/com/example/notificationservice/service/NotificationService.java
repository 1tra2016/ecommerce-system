package com.example.notificationservice.service;

import com.example.notificationservice.dto.event.OrderCreatedEvent;
import com.example.notificationservice.dto.event.OrderUpdateStatusEvent;
import com.example.notificationservice.dto.event.OtpEvent;
import com.example.notificationservice.dto.event.OutboxEventPayload;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.entity.enums.NotificationStatus;
import com.example.notificationservice.entity.enums.NotificationType;
import com.example.notificationservice.repository.NotificationLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Service
@Slf4j
public class NotificationService {

    private final MailService mailService;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(
            MailService mailService,
            NotificationLogRepository notificationLogRepository) {
        this.mailService = mailService;
        this.notificationLogRepository = notificationLogRepository;
    }

    public void handleOtp(OtpEvent event) throws Exception {
        mailService.sendOtp(event.email(), event.otp());
    }

    public void handleOrderCreated(OrderCreatedEvent event) {
        NotificationLog notification = new NotificationLog();
        notification.setEventId(event.eventId());
        notification.setUserId(event.userId());
        notification.setOrderId(event.orderId());
        notification.setOrderNumber(event.orderNumber());
        notification.setRecipient(event.email());
        notification.setType(NotificationType.ORDER_CREATED);
        notification.setTitle("Xác nhận đơn hàng");
        notification.setContent(
                "Đơn hàng " + Objects.toString(event.orderNumber(), "N/A") + " đã được tiếp nhận."
        );
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedAt(
                event.createdAt() == null ? LocalDateTime.now() : event.createdAt()
        );
        notificationLogRepository.save(notification);

        mailService.sendOrderConfirmation(event);
    }

    public void handleOrderUpdateStatus(OrderUpdateStatusEvent event) {
        NotificationLog notification = new NotificationLog();
        notification.setEventId(event.eventId());
        notification.setUserId(event.userId());
        notification.setOrderId(event.orderId());
        notification.setOrderNumber(event.orderNumber());
        notification.setRecipient(event.email());
        notification.setType(NotificationType.ORDER_STATUS_UPDATED);
        notification.setTitle("Cập nhật trạng thái đơn hàng");
        notification.setContent(
                "Đơn hàng " + Objects.toString(event.orderNumber(), "N/A")
                        + " đã chuyển sang trạng thái "
                        + Objects.toString(event.orderStatus(), "N/A") + "."
        );
        notification.setStatus(NotificationStatus.UNREAD);
        if ("PAYMENT_REVIEW".equals(event.orderStatus())) {
            notification.setNote("cần review");
        }
        if ("CANCEL".equals(event.orderStatus())) {
            notification.setNote("Có đơn hàng bị hủy");
        }
        notification.setCreatedAt(
                event.updateAt() == null ? LocalDateTime.now() : event.updateAt()
        );
        notificationLogRepository.save(notification);

        mailService.sendOrderStatusUpdate(event);
    }

    public Page<NotificationLog> getAll(
            Long userId,
            NotificationType type,
            NotificationStatus status,
            String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        Specification<NotificationLog> specification = (
                root,
                query,
                criteriaBuilder
        ) -> {
            var predicates = new ArrayList<Predicate>();
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), toDate));
            }
            if (keyword != null && !keyword.isBlank()) {
                String searchTerm = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")), searchTerm),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("content")), searchTerm),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("orderNumber")), searchTerm),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("recipient")), searchTerm)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(
                    Predicate[]::new));
        };
        return notificationLogRepository.findAll(specification, pageable);
    }

    public void sendTestEmail(String email, String subject, String message) {
        mailService.sendTestEmail(email, subject, message);
    }

    public void handlePaymentReview(OutboxEventPayload event) {
        if (!OutboxEventPayload.PAYMENT_REQUIRES_REVIEW.equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Unexpected event type on payment-review queue: " + event.eventType()
            );
        }

        log.info(
                "Payment review event received; customer email notification is disabled "
                        + "(eventId={}, orderId={}, userId={})",
                event.eventId(),
                event.orderId(),
                event.userId()
        );
    }
}
