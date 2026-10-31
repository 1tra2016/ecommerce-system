package com.example.orderinventoryservice.dto.inventory;

public record InventoryResponse(
        Long id,
        Long variantId,
        String sku,
        Integer availableQuantity,
        Integer reservedQuantity,
        Long version
) {}