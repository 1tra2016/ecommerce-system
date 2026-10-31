package com.example.paymentservice.webhook;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import com.example.paymentservice.dto.payment.stripe.StripeProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.paymentservice.service.IAttemptService;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final StripeProperties stripeProperties;
    private final IAttemptService attemptService;
    private final ObjectMapper objectMapper;

    @Value("${payment.stripe.webhook-secret-file:}")
    private String webhookSecretFile;

    public void handleWebhook(String payload, String signature) {
        Event event;
        String webhookSecret = getWebhookSecret();

        // 1. Xác minh chữ ký trước khi tin tưởng dữ liệu nhận được
        try {
            event = Webhook.constructEvent(
                    payload,
                    signature,
                    webhookSecret

            );
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Stripe webhook signature"
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Stripe webhook payload"
            );
        }

        log.info(
                "Received Stripe webhook: eventId={}, type={}",
                event.getId(),
                event.getType()
        );

        // 2. Chỉ xử lý các sự kiện Checkout cần thiết
        switch (event.getType()) {
            case "checkout.session.completed",
                 "checkout.session.async_payment_succeeded" ->
                    handleSuccessfulCheckout(event);

            case "checkout.session.async_payment_failed" ->
                    handleFailedCheckout(event);

            case "checkout.session.expired" ->
                    handleExpiredCheckout(event);

            default ->
                    log.debug(
                            "Ignoring unsupported Stripe event: {}",
                            event.getType()
                    );
        }
    }

    private String getWebhookSecret() {
        if (webhookSecretFile == null || webhookSecretFile.isBlank()) {
            return stripeProperties.webhookSecret();
        }

        try {
            String secret = Files.readString(
                    Path.of(webhookSecretFile),
                    StandardCharsets.UTF_8
            ).trim();
            if (secret.isEmpty()) {
                throw new IOException("Webhook signing secret file is empty");
            }
            return secret;
        } catch (IOException e) {
            log.error("Cannot read Stripe webhook signing secret from configured file", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe webhook signing secret is unavailable",
                    e
            );
        }
    }

    private void handleSuccessfulCheckout(Event event) {
        log.info("Handling successful checkout event: {}", event.getId());

        CheckoutSessionData session = getSession(event);

        // Checkout completed chưa chắc tiền đã được thanh toán.
        // Chỉ đánh dấu SUCCESS khi Stripe xác nhận payment_status = paid.
        if (!"complete".equals(session.sessionStatus())
                || !"paid".equals(session.paymentStatus())) {
            log.info(
                    "Checkout session is not paid yet: sessionId={}, paymentStatus={}",
                    session.id(),
                    session.paymentStatus()
            );
            return;
        }

        Long attemptId = getAttemptId(session.attemptId());
        Long paymentId = getPaymentId(session.paymentId());

        attemptService.markSucceeded(
                attemptId,
                paymentId,
                session.id(),
                session.paymentStatus()
        );

        log.info(
                "Payment succeeded: attemptId={}, sessionId={}",
                attemptId,
                session.id()
        );
    }

    private void handleFailedCheckout(Event event) {
        CheckoutSessionData session = getSession(event);
        Long attemptId = getAttemptId(session.attemptId());
        Long paymentId = getPaymentId(session.paymentId());

        attemptService.markFailed(
                attemptId,
                paymentId,
                session.id(),
                session.paymentStatus(),
                "STRIPE_ASYNC_PAYMENT_FAILED"
        );

        log.info(
                "Payment attempt failed: attemptId={}, sessionId={}",
                attemptId,
                session.id()
        );
    }

    private void handleExpiredCheckout(Event event) {
        CheckoutSessionData session = getSession(event);
        Long attemptId = getAttemptId(session.attemptId());
        Long paymentId = getPaymentId(session.paymentId());

        attemptService.markExpired(attemptId, paymentId, session.id());
        log.info(
                "Payment attempt expired: attemptId={}, sessionId={}",
                attemptId,
                session.id()
        );
    }

    private CheckoutSessionData getSession(Event event) {
        log.info(
                "Reading Checkout Session from raw event JSON: eventId={}, apiVersion={}",
                event.getId(),
                event.getApiVersion()
        );

        String rawJson = event.getDataObjectDeserializer().getRawJson();
        if (rawJson == null || rawJson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stripe event is missing its Checkout Session object"
            );
        }

        try {
            JsonNode session = objectMapper.readTree(rawJson);
            if (session == null
                    || !session.isObject()
                    || !"checkout.session".equals(
                            session.path("object").asText())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stripe event object is not a Checkout Session"
                );
            }

            String sessionId = session.path("id").asText(null);
            String sessionStatus = session.path("status").asText(null);
            String paymentStatus = session.path("payment_status").asText(null);
            if (sessionId == null || sessionId.isBlank()
                    || sessionStatus == null || sessionStatus.isBlank()
                    || paymentStatus == null || paymentStatus.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stripe Checkout Session is missing required fields"
                );
            }

            return new CheckoutSessionData(
                    sessionId,
                    sessionStatus,
                    paymentStatus,
                    session.path("metadata").path("attemptId").asText(null),
                    session.path("metadata").path("paymentId").asText(null)
            );
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Stripe Checkout Session JSON",
                    ex
            );
        }
    }

    private Long getAttemptId(String attemptId) {
        try {
            if (attemptId == null || attemptId.isBlank()) {
                throw new NumberFormatException("Missing attemptId");
            }

            return Long.parseLong(attemptId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing or invalid attemptId in Stripe metadata"
            );
        }
    }

    private Long getPaymentId(String paymentId) {
        try {
            if (paymentId == null || paymentId.isBlank()) {
                throw new NumberFormatException("Missing paymentId");
            }
            return Long.parseLong(paymentId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing or invalid paymentId in Stripe metadata"
            );
        }
    }

    private record CheckoutSessionData(
            String id,
            String sessionStatus,
            String paymentStatus,
            String attemptId,
            String paymentId
    ) {
    }
}
