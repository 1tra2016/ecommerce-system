package com.example.paymentservice.controller;
import com.example.paymentservice.entity.PaymentAttempt;
import com.example.paymentservice.service.IAttemptService;
import com.example.paymentservice.webhook.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping(
            value = "/webhook",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(
                    value = "Stripe-Signature",
                    required = false
            ) String signature
    ) {
        log.info("Handling Stripe Webhook");
        stripeWebhookService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok(
                "Thanh toán đã hoàn tất. Hệ thống đang xác nhận kết quả."
        );
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> cancel() {
        return ResponseEntity.ok(
                "Bạn đã hủy thao tác thanh toán. Bạn có thể quay lại và thử lại."
        );
    }
}