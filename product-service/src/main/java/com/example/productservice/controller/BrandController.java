package com.example.productservice.controller;

import com.example.productservice.dto.api.ApiResponse;
import com.example.productservice.dto.brand.BrandRequest;
import com.example.productservice.dto.brand.BrandResponse;
import com.example.productservice.entity.Brand;
import com.example.productservice.mapper.BrandMapper;
import com.example.productservice.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BrandResponse>>> getAll(Pageable pageable) {
        Page<Brand> brands = brandService.getAll(pageable);

        Page<BrandResponse> responses = brands.map(BrandMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(
                200, responses, "Brands fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getById(@PathVariable Long id) {
        Brand brand = brandService.getById(id);

        BrandResponse response = BrandMapper.toResponse(brand);

        return ResponseEntity.ok(ApiResponse.success(
                200, response, "Brand fetched successfully"
        ));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> create(
            @Valid @RequestBody BrandRequest request
    ) {
        Brand brand = brandService.create(request);

        BrandResponse response = BrandMapper.toResponse(brand);

        return ResponseEntity.ok(ApiResponse.success(
                200, response, "Brand created successfully"
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request
    ) {
        Brand brand = brandService.update(id, request);

        BrandResponse response = BrandMapper.toResponse(brand);

        return ResponseEntity.ok(ApiResponse.success(
                200, response, "Brand updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        brandService.delete(id);

        return ResponseEntity.ok(ApiResponse.success(
                200, null, "Brand deleted successfully"
        ));
    }
}