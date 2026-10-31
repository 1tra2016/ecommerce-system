package com.example.notificationservice.service;

import com.example.notificationservice.dto.event.OrderCreatedEvent;
import com.example.notificationservice.dto.event.OrderUpdateStatusEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class MailServiceTest {

    private JavaMailSender mailSender;
    private MimeMessage message;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        mailService = new MailService(mailSender);
        ReflectionTestUtils.setField(mailService, "from", "notifications@example.com");
    }

    @Test
    void sendsOrderConfirmationToEventEmail() throws Exception {
        mailService.sendOrderConfirmation(new OrderCreatedEvent(
                "event-1",
                42L,
                "ORD-42",
                7L,
                "customer@example.com",
                new BigDecimal("125.50"),
                "USD",
                LocalDateTime.of(2026, 10, 30, 16, 0),
                LocalDateTime.of(2026, 10, 30, 15, 0)
        ));

        assertEquals("customer@example.com", message.getAllRecipients()[0].toString());
        assertEquals("Order confirmation ORD-42", message.getSubject());
        verify(mailSender).send(message);
    }

    @Test
    void sendsOrderStatusUpdateToEventEmail() throws Exception {
        mailService.sendOrderStatusUpdate(new OrderUpdateStatusEvent(
                "event-2",
                42L,
                "ORD-42",
                "SHIPPED",
                7L,
                "customer@example.com",
                new BigDecimal("125.50"),
                "USD",
                LocalDateTime.of(2026, 10, 30, 16, 0),
                LocalDateTime.of(2026, 10, 30, 15, 0)
        ));

        assertEquals("customer@example.com", message.getAllRecipients()[0].toString());
        assertEquals("Order status update ORD-42", message.getSubject());
        verify(mailSender).send(message);
    }
}
