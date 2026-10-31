package com.example.productservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import com.example.productservice.dto.api.ApiResponse;
import com.example.productservice.dto.productvariant.ProductVariantRequest;
import com.example.productservice.dto.productvariant.ProductVariantResponse;
import com.example.productservice.entity.ProductVariant;
import com.example.productservice.mapper.ProductVariantMapper;
import com.example.productservice.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        ProductVariant variant =
                productVariantService.create(productId, request);

        ProductVariantResponse response =
                ProductVariantMapper.toResponse(variant);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, response, "Tạo biến thể sản phẩm thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductVariantResponse>>> getAll(
            @PathVariable Long productId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ProductVariantResponse> responses =
                productVariantService.searchByProductId(productId, keyword,
                                active, minPrice, maxPrice, pageable)
                .map(ProductVariantMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                responses,
                "Lấy danh sách biến thể sản phẩm thành công"
        ));
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getById(
            @PathVariable Long productId,
            @PathVariable Long variantId
    ) {
        ProductVariant variant =
                productVariantService.getById(productId, variantId);

        ProductVariantResponse response =
                ProductVariantMapper.toResponse(variant);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                response,
                "Lấy thông tin biến thể sản phẩm thành công"
        ));
    }

    @PutMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> update(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        ProductVariant variant =
                productVariantService.update(
                        productId, variantId, request
                );

        ProductVariantResponse response =
                ProductVariantMapper.toResponse(variant);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                response,
                "Cập nhật biến thể sản phẩm thành công"
        ));
    }

    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productId,
            @PathVariable Long variantId
    ) {
        productVariantService.delete(productId, variantId);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                null,
                "Xóa biến thể sản phẩm thành công"
        ));
    }
}
