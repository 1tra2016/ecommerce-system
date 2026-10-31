package com.example.productservice.mapper;

import com.example.productservice.dto.brand.BrandRequest;
import com.example.productservice.dto.brand.BrandResponse;
import com.example.productservice.entity.Brand;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
public final class BrandMapper {

    public static Brand toEntity(BrandRequest request) {
        return Brand.builder()
                .name(request.getName().trim())
                .slug(request.getSlug().trim())
                .logoUrl(request.getLogoUrl())
                .build();
    }

    public static BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getSlug(),
                brand.getLogoUrl()
        );
    }
}
