package com.example.productservice.controller;

import com.example.productservice.dto.api.ApiResponse;
import com.example.productservice.dto.productimage.ProductImageRequest;
import com.example.productservice.dto.productimage.ProductImageResponse;
import com.example.productservice.entity.ProductImage;
import com.example.productservice.mapper.ProductImageMapper;
import com.example.productservice.service.ProductImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageRequest request) {

        ProductImage image =
                productImageService.create(productId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        201,
                        ProductImageMapper.toResponse(image),
                        "Product image created successfully"
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getAll(
            @PathVariable Long productId) {

        List<ProductImageResponse> response =
                productImageService.getAllByProductId(productId)
                        .stream()
                        .map(ProductImageMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        response,
                        "Product images retrieved successfully"
                )
        );
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<ProductImageResponse>>> getPage(
            @PathVariable Long productId,
            Pageable pageable) {

        Page<ProductImageResponse> response =
                productImageService.getPageByProductId(
                                productId, pageable)
                        .map(ProductImageMapper::toResponse);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        response,
                        "Product images retrieved successfully"
                )
        );
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse<ProductImageResponse>> getById(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        ProductImage image =
                productImageService.getById(productId, imageId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        ProductImageMapper.toResponse(image),
                        "Product image retrieved successfully"
                )
        );
    }

    @PutMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> update(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @Valid @RequestBody ProductImageRequest request) {

        ProductImage image =
                productImageService.update(
                        productId, imageId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        ProductImageMapper.toResponse(image),
                        "Product image updated successfully"
                )
        );
    }

    @PatchMapping("/{imageId}/primary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> setPrimary(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        ProductImage image =
                productImageService.setPrimary(productId, imageId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        ProductImageMapper.toResponse(image),
                        "Primary product image updated successfully"
                )
        );
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        productImageService.delete(productId, imageId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        null,
                        "Product image deleted successfully"
                )
        );
    }
}