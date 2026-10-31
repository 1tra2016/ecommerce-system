package com.example.orderinventoryservice.repository;

import com.example.orderinventoryservice.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByVariantId(Long variantId);
    Page<Inventory> findAll(Pageable pageable);

    boolean existsByVariantId(Long variantId);

    boolean existsBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.variantId = :variantId
        """)
    Optional<Inventory> findByVariantIdForUpdate(
            @Param("variantId") Long variantId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.id = :id
        """)
    Optional<Inventory> findByIdForUpdate(
            @Param("id") Long id
    );
}