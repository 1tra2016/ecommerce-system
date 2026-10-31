package com.example.productservice.repository;

import com.example.productservice.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    boolean existsByProductId(Long productId);

    List<ProductImage> findByProductIdOrderBySortOrderAscIdAsc(
            Long productId
    );

    Page<ProductImage> findByProductId(
            Long productId,
            Pageable pageable
    );

    Optional<ProductImage> findByIdAndProductId(
            Long imageId,
            Long productId
    );

    boolean existsByProductIdAndIsPrimaryTrue(Long productId);

    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(
            Long productId
    );

    long deleteByIdAndProductId(Long imageId, Long productId);
}