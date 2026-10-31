package com.example.paymentservice.dto.api;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private LocalDateTime timestamp;
    private String message;
    private String status;
    private int code;
    private T data;

    public static <T> ApiResponse<T> success(int code, T data, String meta) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .code(code)
                .data(data)
                .message(meta)
                .build();
    }

    public static <T> ApiResponse<T> success(int code, T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .code(code)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .code(code)
                .data(data)
                .message(message)
                .build();
    }
}
