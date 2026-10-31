package com.example.userservice.service;

import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        validatePagination(page, size);

        // Chỉ cho phép sắp xếp theo các trường hợp lệ
        if (!"id".equals(sortBy)
                && !"username".equals(sortBy)
                && !"email".equals(sortBy)
                && !"createdAt".equals(sortBy)) {
            sortBy = "createdAt";
        }

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable);
    }

    /**
     * Xem chi tiết tài khoản theo ID.
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));
    }

    /**
     * Tìm kiếm theo username hoặc email.
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(
            String keyword,
            int page,
            int size
    ) {
        validatePagination(page, size);

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException(
                    "Search keyword must not be blank"
            );
        }

        String searchKeyword = keyword.trim();
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        searchKeyword,
                        searchKeyword,
                        pageable
                );
    }

    /**
     * Khóa hoặc mở khóa tài khoản.
     */
    @Transactional
    public User updateUserStatus(Long id, boolean enabled) {
        User user = getUserById(id);

        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    /**
     * Thay đổi quyền USER / ADMIN.
     */
    @Transactional
    public User updateUserRole(Long id, User.Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        User user = getUserById(id);
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Kiểm tra tham số phân trang.
     */
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page index must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }
    }
}
