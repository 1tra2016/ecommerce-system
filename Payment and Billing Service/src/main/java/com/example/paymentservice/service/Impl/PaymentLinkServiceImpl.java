package com.example.paymentservice.service.Impl;

import com.example.paymentservice.dto.payment.PaymentGatewayResult;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.enums.PaymentProvider;
import com.example.paymentservice.exception.exceptions.BusinessException;
import com.example.paymentservice.provider.PaymentGateway;
import com.example.paymentservice.service.IPaymentLinkService;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentLinkServiceImpl implements IPaymentLinkService {

    private final Map<PaymentProvider, PaymentGateway> gateways;

    public PaymentLinkServiceImpl(List<PaymentGateway> gateways) {
        Map<PaymentProvider, PaymentGateway> gatewaysByProvider =
                new EnumMap<>(PaymentProvider.class);
        for (PaymentGateway gateway : gateways) {
            PaymentGateway previous = gatewaysByProvider.put(
                    gateway.getProvider(), gateway);
            if (previous != null) {
                throw new IllegalStateException(
                        "More than one payment gateway registered for "
                                + gateway.getProvider());
            }
        }
        this.gateways = Map.copyOf(gatewaysByProvider);
    }


    @Override
    public PaymentGatewayResult generatePaymentUrl(
            Payment payment,
            PaymentAttempt attempt
    ) {
        if (payment == null || attempt == null) {
            throw new IllegalArgumentException(
                    "Payment and attempt are required"
            );
        }

        if (attempt.getPayment() == null
                || !attempt.getPayment().getId().equals(payment.getId())) {
            throw new IllegalArgumentException(
                    "Attempt does not belong to payment"
            );
        }

        PaymentProvider provider = attempt.getProvider();
        PaymentGateway gateway = gateways.get(provider);

        if (gateway == null) {
            throw new BusinessException(
                    "No payment gateway is configured for " + provider
            );
        }

        return gateway.createPayment(payment, attempt);
    }

}
