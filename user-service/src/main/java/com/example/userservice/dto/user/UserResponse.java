package com.example.userservice.dto.user;

import com.example.userservice.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private User.Role role;
    private Boolean enabled;
}