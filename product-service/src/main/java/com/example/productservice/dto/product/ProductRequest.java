package com.example.productservice.dto.product;

import com.example.productservice.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 200, message = "Slug tối đa 200 ký tự")
    private String slug;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = true,
            message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    private BigDecimal basePrice;

    private ProductStatus status;

    @NotNull(message = "categoryId không được để trống")
    private Long categoryId;

    @NotNull(message = "brandId không được để trống")
    private Long brandId;
}