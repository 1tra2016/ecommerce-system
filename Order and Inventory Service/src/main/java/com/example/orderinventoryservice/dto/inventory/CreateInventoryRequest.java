package com.example.orderinventoryservice.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInventoryRequest(

        @NotNull
        @Positive
        Long variantId,

        @NotNull
        @Min(0)
        Integer availableQuantity
) {}