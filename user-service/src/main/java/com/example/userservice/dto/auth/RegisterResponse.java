package com.example.userservice.dto.auth;

import com.example.userservice.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private User.Role role;
}