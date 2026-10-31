package com.example.userservice.controller;

import com.example.userservice.dto.api.ApiResponse;
import com.example.userservice.dto.user.UpdatePasswordRequest;
import com.example.userservice.dto.user.UpdateProfileRequest;
import com.example.userservice.dto.user.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @RequestHeader("X-User-Id") Long userId
    ) {
        User user = userService.getUserById(userId);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        response,
                        "Get current user successfully"
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = userService.updateProfile(userId, request);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        response,
                        "Profile updated successfully"
                )
        );
    }
    @PutMapping("/me/updatePassword")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(userId,request.getPassword(),request.getNewPassword());
        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Password updated successfully"
                )
        );
    }
}
