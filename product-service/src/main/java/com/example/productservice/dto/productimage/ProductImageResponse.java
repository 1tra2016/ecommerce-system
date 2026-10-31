package com.example.productservice.dto.productimage;

public record ProductImageResponse(
        Long id,
        Long productId,
        String imageUrl,
        Boolean isPrimary,
        Integer sortOrder
) {
}