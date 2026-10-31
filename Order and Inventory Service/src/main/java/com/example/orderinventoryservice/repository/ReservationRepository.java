package com.example.orderinventoryservice.repository;

import com.example.orderinventoryservice.entity.InventoryReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    boolean existsByOrderIdAndVariantId(Long orderId, Long variantId);

    Optional<InventoryReservation> findByOrderIdAndVariantId(
            Long orderId, Long variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM InventoryReservation r
        WHERE r.id = :id
    """)
    Optional<InventoryReservation> findByIdForUpdate(
            @Param("id") Long id);

    List<InventoryReservation> findByOrderId(Long orderId);

    List<InventoryReservation> findByOrderIdOrderByVariantIdAsc(Long orderId);
}
