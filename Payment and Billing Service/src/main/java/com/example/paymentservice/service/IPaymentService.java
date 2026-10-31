package com.example.paymentservice.service;

import com.example.paymentservice.dto.eventmessage.OrderCreatedEvent;
import com.example.paymentservice.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPaymentService {

    void createPaymentFromOrderCreated(OrderCreatedEvent event);

    Payment getPayment(Long paymentId, Long userId);

    Payment getPaymentByOrderNumber(String orderNumber);

    void cancelPayment(Long paymentId, Long userId);

    Page<Payment> getPayments(Pageable pageable);
}