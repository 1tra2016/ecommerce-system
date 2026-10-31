package com.example.productservice.dto.product;

import com.example.productservice.enums.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private ProductStatus status;

    private Long categoryId;
    private String categoryName;

    private Long brandId;
    private String brandName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
