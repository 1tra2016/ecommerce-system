package com.example.productservice.mapper;

import com.example.productservice.enums.ProductStatus;
import com.example.productservice.dto.product.ProductRequest;
import com.example.productservice.dto.product.ProductResponse;
import com.example.productservice.entity.Brand;
import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ProductMapper {

    public static Product toEntity(
            ProductRequest request,
            Brand brand,
            Category category) {

        return Product.builder()
                .name(request.getName().trim())
                .slug(request.getSlug().trim())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(request.getStatus() != null
                        ? request.getStatus()
                        : ProductStatus.DRAFT)
                .brand(brand)
                .category(category)
                .build();
    }

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .brandId(product.getBrand().getId())
                .brandName(product.getBrand().getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
