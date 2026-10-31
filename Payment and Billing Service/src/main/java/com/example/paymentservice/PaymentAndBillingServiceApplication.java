package com.example.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@EnableScheduling
@SpringBootApplication
public class PaymentAndBillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentAndBillingServiceApplication.class, args);
    }

}
