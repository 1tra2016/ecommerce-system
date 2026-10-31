package com.example.productservice.mapper;

import com.example.productservice.dto.productvariant.ProductVariantRequest;
import com.example.productservice.dto.productvariant.ProductVariantResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.ProductVariant;

public class ProductVariantMapper {

    private ProductVariantMapper() {}

    public static ProductVariant toEntity(
            ProductVariantRequest request,
            Product product
    ) {
        return ProductVariant.builder()
                .product(product)
                .sku(request.getSku().trim())
                .attributes(request.getAttributes())
                .price(request.getPrice())
                .active(request.getActive() != null
                        ? request.getActive()
                        : true)
                .build();
    }

    public static ProductVariantResponse toResponse(
            ProductVariant variant
    ) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getProduct().getId(),
                variant.getSku(),
                variant.getAttributes(),
                variant.getPrice(),
                variant.getActive()
        );
    }
}
