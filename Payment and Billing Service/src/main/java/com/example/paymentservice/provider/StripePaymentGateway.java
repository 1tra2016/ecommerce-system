package com.example.paymentservice.provider;

import com.example.paymentservice.dto.payment.PaymentGatewayResult;
import com.example.paymentservice.dto.payment.stripe.StripeProperties;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.entity.enums.PaymentProvider;
import com.example.paymentservice.exception.exceptions.PaymentGatewayException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
public class StripePaymentGateway implements PaymentGateway {

    private final StripeProperties properties;

    public StripePaymentGateway(StripeProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentGatewayResult createPayment(
            Payment payment,
            PaymentAttempt attempt
    ) {
        if (payment.getAmount() == null
                || payment.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be positive"
            );
        }

        try {
            // Stripe dùng đơn vị VND nguyên, không nhân 100.
            long amount = payment.getAmount().longValueExact();

            String currency = payment.getCurrency()
                    .toLowerCase(Locale.ROOT);

            log.info("Creating Stripe Payment");
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(
                                    properties.successUrl() + "?session_id={CHECKOUT_SESSION_ID}"
                            )
                            .setCancelUrl(properties.cancelUrl())
                            .setClientReferenceId(payment.getOrderId().toString()
                            )
                            .putMetadata("paymentId", payment.getId().toString())
                            .putMetadata("attemptId", attempt.getId().toString())
                            .putMetadata("orderId", payment.getOrderId().toString())
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams
                                                            .LineItem.PriceData
                                                            .builder()
                                                            .setCurrency(currency)
                                                            .setUnitAmount(amount)
                                                            .setProductData(
                                                                    SessionCreateParams
                                                                            .LineItem
                                                                            .PriceData
                                                                            .ProductData
                                                                            .builder()
                                                                            .setName(
                                                                                    "Restaurant order #"
                                                                                            + payment.getOrderId()
                                                                            )
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            RequestOptions requestOptions =
                    RequestOptions.builder()
                            .setApiKey(properties.secretKey())
                            .setIdempotencyKey(
                                    attempt.getIdempotencyKey()
                            )
                            .build();

            Session session = Session.create(params, requestOptions);


            return new PaymentGatewayResult(
                    session.getUrl(),
                    session.getId(),
                    session.getPaymentStatus()
            );

        } catch (StripeException ex) {
            throw new PaymentGatewayException(
                    "Cannot create Stripe Checkout Session. "+ ex.toString()
            );
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Payment amount must be a whole number of VND",
                    ex
            );
        }
    }
}
