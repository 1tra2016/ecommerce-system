package com.example.productservice.mapper;

import com.example.productservice.dto.category.CategoryRequest;
import com.example.productservice.dto.category.CategoryResponse;
import com.example.productservice.entity.Category;
import lombok.AllArgsConstructor;

import java.time.format.DateTimeFormatter;

@AllArgsConstructor
public final class CategoryMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName().trim())
                .slug(request.getSlug().trim())
                .description(request.getDescription())
                .build();
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getCreatedAt().format(DATE_TIME_FORMATTER)
        );
    }
}
