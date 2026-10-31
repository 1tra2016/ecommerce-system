package com.example.orderinventoryservice.controller;

import com.example.orderinventoryservice.dto.inventory.CreateInventoryRequest;
import com.example.orderinventoryservice.dto.inventory.InventoryResponse;
import com.example.orderinventoryservice.dto.inventory.UpdateInventoryRequest;
import com.example.orderinventoryservice.entity.Inventory;
import com.example.orderinventoryservice.mapper.InventoryMapper;
import com.example.orderinventoryservice.service.InventoryService;
import com.example.orderinventoryservice.dto.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryRequest request) {

        Inventory inv = inventoryService.createInventory(request);
        InventoryResponse res = InventoryMapper.toResponse(inv);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        res,
                        "Tạo thành công"
                )
        );
    }

    // READ ALL
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getAllInventoriesPage(Pageable pageable) {
        Page<InventoryResponse> res = inventoryService.getAllInventories(pageable)
                .map(InventoryMapper::toResponse);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        res,
                        "Danh sách inventory (phân trang)"
                )
        );
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable Long id) {
        Inventory inv = inventoryService.getInventoryById(id);
        InventoryResponse res = InventoryMapper.toResponse(inv);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        res,
                        "Inventory theo ID"
                )
        );
    }

    // READ BY VARIANT ID
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByVariantId(@PathVariable Long variantId) {
        Inventory inv = inventoryService.getInventoryByVariantId(variantId);
        InventoryResponse res = InventoryMapper.toResponse(inv);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        res,
                        "Inventory theo Variant ID"
                )
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request) {

        Inventory inv = inventoryService.updateInventory(id, request);
        InventoryResponse res = InventoryMapper.toResponse(inv);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        res,
                        "Cập nhật thành công"
                )
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.success(
                        HttpStatus.NO_CONTENT.value(),
                        null,
                        "Xóa thành công"
                )
        );
    }
}
