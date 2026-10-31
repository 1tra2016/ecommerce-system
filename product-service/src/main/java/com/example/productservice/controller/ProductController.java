
package com.example.productservice.controller;

import com.example.productservice.dto.api.ApiResponse;
import com.example.productservice.dto.product.ProductRequest;
import com.example.productservice.dto.product.ProductResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.enums.ProductStatus;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

        private final ProductService productService;

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<ProductResponse>> create(
                        @Valid @RequestBody ProductRequest request) {

                Product product = productService.create(request);
                ProductResponse response = ProductMapper.toResponse(product);

                return ResponseEntity.ok(ApiResponse.success(
                                201, response, "Product created successfully"));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Long brandId,
                        @RequestParam(required = false) ProductStatus status,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<ProductResponse> response = productService.search(keyword, categoryId, brandId, status,
                                minPrice, maxPrice, pageable)
                                .map(ProductMapper::toResponse);

                return ResponseEntity.ok(ApiResponse.success(200, response));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<ProductResponse>> getById(
                        @PathVariable Long id) {

                Product product = productService.getById(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                200,
                                                ProductMapper.toResponse(product),
                                                "Product retrieved successfully"));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<ProductResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody ProductRequest request) {

                Product product = productService.update(id, request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                200,
                                                ProductMapper.toResponse(product),
                                                "Product updated successfully"));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Long id) {

                productService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                200, null, "Product deleted successfully"));
        }
}
