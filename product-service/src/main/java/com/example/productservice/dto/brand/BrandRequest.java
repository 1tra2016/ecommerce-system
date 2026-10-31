package com.example.productservice.dto.brand;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 150, message = "Slug must be at most 150 characters")
    private String slug;

    @Size(max = 255, message = "Logo URL must be at most 255 characters")
    private String logoUrl;
}