package com.example.userservice.dto.user;

import com.example.userservice.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull(message = "Role must not be null")
    private User.Role role;
}