package com.example.orderinventoryservice.service;

import com.example.orderinventoryservice.client.product.ProductGrpcClient;
import com.example.orderinventoryservice.client.product.ProductInfo;
import com.example.orderinventoryservice.dto.inventory.CreateInventoryRequest;
import com.example.orderinventoryservice.dto.inventory.InventoryResponse;
import com.example.orderinventoryservice.dto.inventory.UpdateInventoryRequest;
import com.example.orderinventoryservice.entity.Inventory;
import com.example.orderinventoryservice.exception.exceptions.BusinessException;
import com.example.orderinventoryservice.exception.exceptions.ConflictException;
import com.example.orderinventoryservice.exception.exceptions.ResourceNotFoundException;
import com.example.orderinventoryservice.mapper.InventoryMapper;
import com.example.orderinventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductGrpcClient productGrpcClient;

    // CREATE
    @Transactional
    public Inventory createInventory(
            CreateInventoryRequest request) {

        ProductInfo product = productGrpcClient.getVariants(java.util.Set.of(request.variantId()))
                .get(request.variantId());
        if (product == null || !product.active()) {
            throw new BusinessException("Product variant is missing or inactive: " + request.variantId());
        }

        if (inventoryRepository.existsByVariantId(request.variantId())) {
            throw new BusinessException(
                    "Inventory already exists for variant: "
                            + request.variantId()
            );
        }

        Inventory inventory = Inventory.builder()
                .variantId(request.variantId())
                .sku(product.sku())
                .availableQuantity(request.availableQuantity())
                .reservedQuantity(0)
                .build();

        return inventoryRepository.save(inventory);
    }

    // READ ALL
    @Transactional(readOnly = true)
    public Page<Inventory> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

    // READ BY ID
    @Transactional(readOnly = true)
    public Inventory getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + id));
        return inventory;
    }

    // READ BY VARIANT ID
    @Transactional(readOnly = true)
    public Inventory getInventoryByVariantId(Long variantId) {
        Inventory inventory = inventoryRepository
                .findByVariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for variant: " + variantId));

        return inventory;
    }

    // UPDATE
    @Transactional
    public Inventory updateInventory(Long id, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + id));

        String newSku = request.sku().trim();

        if (!inventory.getSku().equals(newSku) && inventoryRepository.existsBySku(newSku)) {
            throw new ConflictException("SKU already exists: " + newSku);
        }

        inventory.setSku(newSku);

        // Chỉ điều chỉnh lượng hàng khả dụng.
        // Không thay đổi reservedQuantity.
        inventory.setAvailableQuantity(request.availableQuantity());

        return inventoryRepository.save(inventory);
    }

    // DELETE
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + id));

        if (inventory.getReservedQuantity() != 0) {
            throw new BusinessException("Cannot delete inventory with reserved stock");
        }
        if (inventory.getAvailableQuantity() != 0) {
            throw new BusinessException("Cannot delete inventory while available stock remains");
        }

        inventoryRepository.delete(inventory);
    }

    // RESERVE STOCK — giữ nguyên PESSIMISTIC_WRITE
    @Transactional
    public void reserveStock(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }

        Inventory inventory = inventoryRepository
                .findByVariantIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for variant: " + variantId));

        if (inventory.getAvailableQuantity() < quantity) {throw new BusinessException("Insufficient stock");}

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);

        // Entity managed: Hibernate tự cập nhật khi commit.
    }
}
