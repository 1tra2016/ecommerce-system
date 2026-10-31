package com.example.notificationservice.controller;

import com.example.notificationservice.dto.api.SendTestEmailRequest;
import com.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationControllerTest {

    private NotificationService notificationService;
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        controller = new NotificationController(notificationService);
    }

    @Test
    void sendsTestEmailAndReturnsSuccess() {
        SendTestEmailRequest request = new SendTestEmailRequest(
                "customer@example.com",
                "Notification test",
                "This is a test email."
        );

        ResponseEntity<?> response = controller.sendTestEmail(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).sendTestEmail(
                "customer@example.com",
                "Notification test",
                "This is a test email."
        );
    }
}
