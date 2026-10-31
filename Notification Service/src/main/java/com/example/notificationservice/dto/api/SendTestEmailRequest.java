package com.example.notificationservice.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendTestEmailRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 200)
        String subject,

        @NotBlank
        @Size(max = 5000)
        String message
) {
}
