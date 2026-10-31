package com.example.orderinventoryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${app.messaging.payment-succeeded-queue}")
    private String paymentSucceededQueue;
    @Value("${app.messaging.payment-review-queue}")
    private String paymentReviewQueue;

    @Value("${app.messaging.order-exchange}")
    private String orderExchange;

    @Value("${app.messaging.notification-order-created-queue}")
    private String notificationOrderCreatedQueue;

    @Value("${app.messaging.order-created-routing-key}")
    private String orderCreatedRoutingKey;

    @Bean
    public Queue paymentSucceededQueue() { return new Queue(paymentSucceededQueue, true); }
    @Bean
    public Queue paymentReviewQueue() { return new Queue(paymentReviewQueue, true); }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange, true, false);
    }

    @Bean
    public Queue notificationOrderCreatedQueue() {
        return new Queue(notificationOrderCreatedQueue, true);
    }

    @Bean
    public Binding notificationOrderCreatedBinding() {
        return BindingBuilder
                .bind(notificationOrderCreatedQueue())
                .to(orderExchange())
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}