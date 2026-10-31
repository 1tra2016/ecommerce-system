package com.example.notificationservice.controller;

import com.example.notificationservice.dto.api.ApiResponse;
import com.example.notificationservice.dto.api.SendTestEmailRequest;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.entity.enums.NotificationStatus;
import com.example.notificationservice.entity.enums.NotificationType;
import com.example.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationLog>>> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<NotificationLog> notifications = notificationService.getAll(
                userId, type, status, keyword, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, notifications));
    }

    @PostMapping("/testmail")
    public ResponseEntity<ApiResponse<Void>> sendTestEmail(
            @Valid @RequestBody SendTestEmailRequest request) {
        System.out.println("Sending test email");
        notificationService.sendTestEmail(
                request.email(),
                request.subject(),
                request.message()
        );

        return ResponseEntity.ok(
                ApiResponse.success(200, null, "Test email sent successfully")
        );
    }
}
