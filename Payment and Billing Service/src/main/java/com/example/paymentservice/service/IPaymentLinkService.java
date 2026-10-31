package com.example.paymentservice.service;

import com.example.paymentservice.dto.payment.PaymentGatewayResult;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentAttempt;

public interface IPaymentLinkService {

    PaymentGatewayResult generatePaymentUrl(
            Payment payment,
            PaymentAttempt attempt);
}