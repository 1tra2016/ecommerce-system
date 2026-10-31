package com.example.productservice.dto.brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BrandResponse {
    Long id;
    String name;
    String slug;
    String logoUrl;
}
