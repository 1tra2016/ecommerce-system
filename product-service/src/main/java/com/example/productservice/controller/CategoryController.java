package com.example.productservice.controller;

import com.example.productservice.dto.api.ApiResponse;
import com.example.productservice.dto.category.CategoryRequest;
import com.example.productservice.dto.category.CategoryResponse;
import com.example.productservice.entity.Category;
import com.example.productservice.mapper.CategoryMapper;
import com.example.productservice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        Category category = categoryService.createCategory(request);

        CategoryResponse response = CategoryMapper.toResponse(category);

        return ResponseEntity.ok(ApiResponse.success(
                201, response, "Category created successfully"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCategories(
            Pageable pageable
    ) {
        Page<CategoryResponse> responses = categoryService.getAllCategories(pageable)
                .map(CategoryMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(
                200, responses, "Categories fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long id
    ) {
        Category category = categoryService.getCategoryById(id);

        CategoryResponse response = CategoryMapper.toResponse(category);

        return ResponseEntity.ok(ApiResponse.success(
                200, response, "Category fetched successfully"
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        Category category = categoryService.updateCategory(id, request);

        CategoryResponse response = CategoryMapper.toResponse(category);

        return ResponseEntity.ok(ApiResponse.success(
                200, response, "Category updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id
    ) {
        categoryService.deleteCategory(id);

        return ResponseEntity.ok(ApiResponse.success(
                200, null, "Category deleted successfully"
        ));
    }
}