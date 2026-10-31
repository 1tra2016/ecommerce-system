package com.example.notificationservice.service;

import com.example.notificationservice.dto.event.OrderUpdateStatusEvent;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.entity.enums.NotificationStatus;
import com.example.notificationservice.entity.enums.NotificationType;
import com.example.notificationservice.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationServiceTest {

    private MailService mailService;
    private NotificationLogRepository notificationLogRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        mailService = mock(MailService.class);
        notificationLogRepository = mock(NotificationLogRepository.class);
        notificationService = new NotificationService(mailService, notificationLogRepository);
    }

    @Test
    void savesPaymentReviewNoteAndSendsOrderStatusEmail() {
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 31, 1, 0);
        OrderUpdateStatusEvent event = new OrderUpdateStatusEvent(
                "event-1",
                42L,
                "ORD-42",
                "PAYMENT_REVIEW",
                7L,
                "customer@example.com",
                null,
                "VND",
                updatedAt,
                updatedAt
        );

        notificationService.handleOrderUpdateStatus(event);

        ArgumentCaptor<NotificationLog> notificationCaptor =
                ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(notificationCaptor.capture());
        NotificationLog notification = notificationCaptor.getValue();
        assertEquals("cần review", notification.getNote());
        assertEquals(NotificationType.ORDER_STATUS_UPDATED, notification.getType());
        assertEquals(NotificationStatus.UNREAD, notification.getStatus());
        assertEquals(7L, notification.getUserId());
        assertEquals(42L, notification.getOrderId());
        assertEquals(updatedAt, notification.getCreatedAt());
        verify(mailService).sendOrderStatusUpdate(event);
    }

    @Test
    void leavesNoteEmptyForOtherOrderStatuses() {
        OrderUpdateStatusEvent event = new OrderUpdateStatusEvent(
                "event-2",
                42L,
                "ORD-42",
                "SHIPPED",
                7L,
                "customer@example.com",
                null,
                "VND",
                null,
                null
        );

        notificationService.handleOrderUpdateStatus(event);

        ArgumentCaptor<NotificationLog> notificationCaptor =
                ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(notificationCaptor.capture());
        assertNull(notificationCaptor.getValue().getNote());
        verify(mailService).sendOrderStatusUpdate(event);
    }
}
