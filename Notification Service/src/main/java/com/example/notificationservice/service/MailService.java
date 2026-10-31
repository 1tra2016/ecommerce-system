package com.example.notificationservice.service;

import com.example.notificationservice.dto.event.OrderCreatedEvent;
import com.example.notificationservice.dto.event.OrderUpdateStatusEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class MailService {

    private static final DateTimeFormatter EMAIL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String otp)
            throws MessagingException {

        MimeMessage message =
                mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject("Password Verification OTP");

        String html = """
            <div style="font-family: Arial">
                <h2>Password Verification</h2>
                <p>Your OTP is:</p>
                <h1>%s</h1>
                <p>This code will expire in 1 minute.</p>
                <p>Please do not share this code with anyone.</p>
            </div>
            """.formatted(otp);

        helper.setText(html, true);

        mailSender.send(message);
    }

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        String html = """
                <div style="font-family: Arial, sans-serif">
                    <h2>Order confirmation</h2>
                    <p>Your order has been received.</p>
                    <p><strong>Order number:</strong> %s</p>
                    <p><strong>Total:</strong> %s</p>
                    <p><strong>Payment deadline:</strong> %s</p>
                </div>
                """.formatted(
                escape(event.orderNumber()),
                escape(formatAmount(event.totalAmount(), event.currency())),
                escape(formatDateTime(event.paymentDeadline()))
        );

        sendHtml(
                event.email(),
                "Order confirmation " + Objects.toString(event.orderNumber(), ""),
                html
        );

    }

    public void sendOrderStatusUpdate(OrderUpdateStatusEvent event) {
        String html = """
                <div style="font-family: Arial, sans-serif">
                    <h2>Order status update</h2>
                    <p>The status of your order has changed.</p>
                    <p><strong>Order number:</strong> %s</p>
                    <p><strong>Status:</strong> %s</p>
                    <p><strong>Total:</strong> %s</p>
                    <p><strong>Updated at:</strong> %s</p>
                </div>
                """.formatted(
                escape(event.orderNumber()),
                escape(event.orderStatus()),
                escape(formatAmount(event.totalAmount(), event.currency())),
                escape(formatDateTime(event.updateAt()))
        );

        sendHtml(
                event.email(),
                "Order status update " + Objects.toString(event.orderNumber(), ""),
                html
        );
    }

    public void sendTestEmail(String email, String subject, String message) {
        String html = """
                <div style="font-family: Arial, sans-serif">
                    <h2>%s</h2>
                    <p>%s</p>
                </div>
                """.formatted(escape(subject), escape(message).replace("\n", "<br>"));

        sendHtml(email, subject, html);
    }

    private void sendHtml(
            String email,
            String subject,
            String html) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email recipient must not be blank");
        }

        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException(
                    "Cannot create email message", e
            );
        }
    }

    private String formatAmount(
            BigDecimal amount,
            String currency) {
        String formattedAmount = amount == null ? "N/A" : amount.toPlainString();
        return currency == null || currency.isBlank()
                ? formattedAmount
                : formattedAmount + " " + currency;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "N/A" : EMAIL_DATE_FORMAT.format(value);
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(Objects.toString(value, "N/A"));
    }
}