package com.example.userservice.controller;

import com.example.userservice.dto.auth.LoginRequest;
import com.example.userservice.dto.auth.RegisterRequest;
import com.example.userservice.dto.api.ApiResponse;
import com.example.userservice.dto.api.JwtResponse;
import com.example.userservice.dto.auth.RegisterResponse;
import com.example.userservice.entity.User;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);

        RegisterResponse response = RegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        201,
                        response,
                        "Register successfully"
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        User user = authService.login(request);
        String token = jwtService.generateToken(user);

        // JWT sẽ được tạo ở đây sau khi hoàn thành JwtService
        JwtResponse response = JwtResponse.builder()
                .username(user.getUsername())
                .type("Bearer")
                .accessToken(token)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        response,
                        "Login successfully"
                )
        );
    }
}