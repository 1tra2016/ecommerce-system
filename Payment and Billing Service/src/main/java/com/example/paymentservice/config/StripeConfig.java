package com.example.paymentservice.config;

import com.example.paymentservice.dto.payment.stripe.StripeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfig {
}