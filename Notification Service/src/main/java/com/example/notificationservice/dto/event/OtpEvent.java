package com.example.notificationservice.dto.event;

public record OtpEvent(
        String email,
        String otp,
        Integer expiryMinutes
) {}