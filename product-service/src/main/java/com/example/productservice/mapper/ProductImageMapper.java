package com.example.productservice.mapper;

import com.example.productservice.dto.productimage.ProductImageRequest;
import com.example.productservice.dto.productimage.ProductImageResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.ProductImage;

public final class ProductImageMapper {

    private ProductImageMapper() {
    }

    public static ProductImage toEntity(
            ProductImageRequest request,
            Product product) {

        return ProductImage.builder()
                .product(product)
                .imageUrl(request.getImageUrl().trim())
                .sortOrder(
                        request.getSortOrder() != null
                                ? request.getSortOrder()
                                : 0
                )
                .build();
    }

    public static ProductImageResponse toResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getImageUrl(),
                image.getIsPrimary(),
                image.getSortOrder()
        );
    }
}