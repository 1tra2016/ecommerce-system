package com.example.orderinventoryservice.service;

import com.example.orderinventoryservice.entity.Inventory;
import com.example.orderinventoryservice.entity.InventoryReservation;
import com.example.orderinventoryservice.entity.Order;
import com.example.orderinventoryservice.entity.enums.ReservationStatus;
import com.example.orderinventoryservice.exception.exceptions.BusinessException;
import com.example.orderinventoryservice.exception.exceptions.DuplicateResourceException;
import com.example.orderinventoryservice.exception.exceptions.ResourceNotFoundException;
import com.example.orderinventoryservice.repository.InventoryRepository;
import com.example.orderinventoryservice.repository.OrderRepository;
import com.example.orderinventoryservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    /**
     * Giữ hàng cho một đơn hàng.
     */
    @Transactional
    public InventoryReservation reserve(
            Long orderId,
            Long variantId,
            Integer quantity) {

        if (orderId == null || orderId <= 0) {
            throw new BusinessException("Invalid order ID");
        }

        if (variantId == null || variantId <= 0) {
            throw new BusinessException("Invalid variant ID");
        }

        if (quantity == null || quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }

        // Khóa inventory trước để đồng bộ các thao tác trên cùng biến thể.
        Inventory inventory = inventoryRepository
                .findByVariantIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for variant: " + variantId));

        // Kiểm tra sau khi khóa inventory để tránh hai request
        // đồng thời tạo reservation cho cùng orderId/variantId.
        if (reservationRepository.existsByOrderIdAndVariantId(orderId, variantId)) {
            throw new DuplicateResourceException("Reservation already exists for this order and variant");
        }

        if (inventory.getAvailableQuantity() < quantity) {
            throw new BusinessException("Insufficient stock for variant: " + variantId);
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);

        InventoryReservation reservation = InventoryReservation.builder()
                .orderId(orderId)
                .variantId(variantId)
                .quantity(quantity)
                .status(ReservationStatus.RESERVED)
                .build();

        // Cả thay đổi inventory và reservation cùng transaction.
        return reservationRepository.save(reservation);
    }

    /**
     * Xác nhận hàng đã giữ, ví dụ khi đơn hàng được xác nhận thanh toán.
     */
    @Transactional
    public InventoryReservation confirm(Long reservationId) {

        InventoryReservation reservation = getOrderThenReservationForUpdate(reservationId);
        Order order = orderRepository.findById(reservation.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for reservation: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return reservation;
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BusinessException("Only RESERVED reservations can be confirmed");
        }
        if (order.getPaymentDeadline() == null
                || !order.getPaymentDeadline().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Order payment deadline has expired");
        }

        Inventory inventory = inventoryRepository
                .findByVariantIdForUpdate(reservation.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for variant: "
                                + reservation.getVariantId()));

        if (inventory.getReservedQuantity() < reservation.getQuantity()) {
            throw new BusinessException(
                    "Reserved stock is inconsistent for variant: "
                            + reservation.getVariantId());
        }

        // Hàng đã bán: không còn thuộc số lượng đang giữ.
        inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());

        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservation;
    }

    /**
     * Giải phóng hàng khi đơn bị hủy hoặc thanh toán thất bại.
     */
    @Transactional
    public InventoryReservation release(Long reservationId) {

        InventoryReservation reservation = getOrderThenReservationForUpdate(reservationId);

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return reservation;
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BusinessException("Only RESERVED reservations can be released");
        }

        Inventory inventory = inventoryRepository
                .findByVariantIdForUpdate(reservation.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for variant: "
                                + reservation.getVariantId()));

        int quantity = reservation.getQuantity();

        if (inventory.getReservedQuantity() < quantity) {
            throw new BusinessException(
                    "Reserved stock is inconsistent for variant: "
                            + reservation.getVariantId());
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);

        reservation.setStatus(ReservationStatus.RELEASED);
        return reservation;
    }

    @Transactional(readOnly = true)
    public boolean canConfirmOrderReservations(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        List<InventoryReservation> reservations =
                reservationRepository.findByOrderIdOrderByVariantIdAsc(orderId);
        if (reservations.isEmpty()
                || order.getPaymentDeadline() == null
                || !order.getPaymentDeadline().isAfter(LocalDateTime.now())) {
            return false;
        }
        return reservations.stream().allMatch(reservation ->
                reservation.getStatus() == ReservationStatus.CONFIRMED
                        || reservation.getStatus() == ReservationStatus.RESERVED);
    }
    @Transactional
    public void confirmOrderReservations(Long orderId) {
        for (InventoryReservation reservation : reservationRepository
                .findByOrderIdOrderByVariantIdAsc(orderId)) {
            confirm(reservation.getId());
        }
    }

    @Transactional
    public void releaseOrderReservations(Long orderId) {
        for (InventoryReservation reservation : reservationRepository
                .findByOrderIdOrderByVariantIdAsc(orderId)) {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                release(reservation.getId());
            }
        }
    }

    @Transactional
    public void expireOrderReservations(Long orderId) {
        for (InventoryReservation candidate : reservationRepository
                .findByOrderIdOrderByVariantIdAsc(orderId)) {
            InventoryReservation reservation = getReservationForUpdate(candidate.getId());
            if (reservation.getStatus() != ReservationStatus.RESERVED) {
                continue;
            }
            Inventory inventory = inventoryRepository
                    .findByVariantIdForUpdate(reservation.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for variant: " + reservation.getVariantId()));
            if (inventory.getReservedQuantity() < reservation.getQuantity()) {
                System.out.println("Số lượng không đủ bù. Chắc chắn có lỗi");
            }
            inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + reservation.getQuantity());
            reservation.setStatus(ReservationStatus.EXPIRED);
        }
    }

    @Transactional(readOnly = true)
    public InventoryReservation getById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + reservationId));
    }

    private InventoryReservation getReservationForUpdate(Long reservationId) {

        return reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + reservationId));
    }

    /**
     * Locks rows using the shared order: Order -> Reservation -> Inventory.
     * The initial reservation lookup is intentionally unlocked; it only obtains
     * the immutable order ID needed to acquire the first lock.
     */
    private InventoryReservation getOrderThenReservationForUpdate(Long reservationId) {
        InventoryReservation reference = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + reservationId));

        Order order = orderRepository.findByIdForUpdate(reference.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for reservation: " + reservationId));

        InventoryReservation reservation = getReservationForUpdate(reservationId);
        if (!reservation.getOrderId().equals(order.getId())) {
            throw new BusinessException("Reservation order changed while acquiring locks");
        }
        return reservation;
    }
}
