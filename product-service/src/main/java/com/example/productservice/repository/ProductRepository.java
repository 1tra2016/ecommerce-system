package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import com.example.productservice.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("""
            SELECT p FROM Product p
            WHERE (LOWER(p.name) LIKE :keywordPattern
                OR LOWER(p.slug) LIKE :keywordPattern
                OR LOWER(p.description) LIKE :keywordPattern
                OR LOWER(p.brand.name) LIKE :keywordPattern
                OR LOWER(p.category.name) LIKE :keywordPattern)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:brandId IS NULL OR p.brand.id = :brandId)
              AND (:status IS NULL OR p.status = :status)
              AND (:minPrice IS NULL OR p.basePrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)
            """)
    Page<Product> search(
            @Param("keywordPattern") String keywordPattern,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("status") ProductStatus status,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"category", "brand"})
    Optional<Product> findById(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);
}
