package com.example.userservice.controller;

import com.example.userservice.dto.user.AdminUserResponse;
import com.example.userservice.dto.user.UpdateUserRoleRequest;
import com.example.userservice.dto.user.UpdateUserStatusRequest;
import com.example.userservice.entity.User;
import com.example.userservice.service.AdminUserService;
import com.example.userservice.dto.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    //Lấy danh sách tài khoản có phân trang
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<AdminUserResponse> result =
                adminUserService.getAllUsers(page, size, sortBy, direction)
                        .map(AdminUserResponse::from);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200, result, "Get users successfully"
                )
        );
    }

    //Xem chi tiết tài khoản
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(
            @PathVariable Long id
    ) {
        User user = adminUserService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        AdminUserResponse.from(user),
                        "Get user successfully"
                )
        );
    }

    //Tìm kiếm theo username hoặc email.
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AdminUserResponse> result =
                adminUserService.searchUsers(keyword, page, size)
                        .map(AdminUserResponse::from);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200, result, "Search users successfully"
                )
        );
    }

    //Khóa hoặc mở khóa tài khoản.
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        User user = adminUserService.updateUserStatus(
                id, request.getEnabled()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        AdminUserResponse.from(user),
                        request.getEnabled()
                                ? "User enabled successfully"
                                : "User disabled successfully"
                )
        );
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        User user = adminUserService.updateUserRole(
                id, request.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        AdminUserResponse.from(user),
                        "User role updated successfully"
                )
        );
    }
}
