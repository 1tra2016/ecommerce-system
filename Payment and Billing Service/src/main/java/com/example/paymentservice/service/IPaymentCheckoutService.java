package com.example.paymentservice.service;

import com.example.paymentservice.dto.payment.CreatePaymentResponse;

public interface IPaymentCheckoutService {

    CreatePaymentResponse checkout(
            String orderNumber,
            Long userId
    );
}