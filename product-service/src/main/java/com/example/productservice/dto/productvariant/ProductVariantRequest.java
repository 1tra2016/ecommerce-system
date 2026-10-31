package com.example.productservice.dto.productvariant;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductVariantRequest {

    @NotBlank(message = "SKU không được để trống")
    @Size(max = 100, message = "SKU không được vượt quá 100 ký tự")
    private String sku;

    private String attributes;

    @NotNull(message = "Giá variant không được để trống")
    @DecimalMin(value = "0.0", message = "Giá không được âm")
    @Digits(integer = 10, fraction = 2,
            message = "Giá tối đa 10 chữ số nguyên và 2 chữ số thập phân")
    private BigDecimal price;

    private Boolean active;
}