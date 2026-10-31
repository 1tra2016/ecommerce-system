package com.example.orderinventoryservice.client.product;

import java.math.BigDecimal;

public record ProductInfo(
        Long productId,
        Long variantId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        boolean active,
        boolean sellable
) {
}
