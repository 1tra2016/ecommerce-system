package com.example.paymentservice.provider;

import com.example.paymentservice.dto.payment.PaymentGatewayResult;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.enums.PaymentProvider;

/** Adapter contract implemented by each payment provider integration. */
public interface PaymentGateway {

    PaymentProvider getProvider();

    PaymentGatewayResult createPayment(
            Payment payment,
            PaymentAttempt attempt
    );

}
