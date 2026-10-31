package com.example.productservice.dto.productvariant;

import java.math.BigDecimal;

public record ProductVariantResponse(
        Long id,
        Long productId,
        String sku,
        String attributes,
        BigDecimal price,
        Boolean active
) {}