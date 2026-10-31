package com.example.orderinventoryservice.mapper;

import com.example.orderinventoryservice.dto.inventory.InventoryResponse;
import com.example.orderinventoryservice.entity.Inventory;

public final class InventoryMapper {
    public static InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getVariantId(),
                inventory.getSku(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getVersion()
        );
    }
}
