package com.example.productservice.repository;

import com.example.productservice.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findByIdAndProductId(Long variantId, Long productId);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    Page<ProductVariant> findByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = {"product"})
    @Query("""
            SELECT v FROM ProductVariant v
            WHERE v.product.id = :productId
              AND LOWER(v.sku) LIKE :keywordPattern
              AND (:active IS NULL OR v.active = :active)
              AND (:minPrice IS NULL OR v.price >= :minPrice)
              AND (:maxPrice IS NULL OR v.price <= :maxPrice)
            """)
    Page<ProductVariant> searchByProductId(
            @Param("productId") Long productId,
            @Param("keywordPattern") String keywordPattern,
            @Param("active") Boolean active,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @EntityGraph(attributePaths = {"product"})
    List<ProductVariant> findByIdIn(Collection<Long> ids);

}
