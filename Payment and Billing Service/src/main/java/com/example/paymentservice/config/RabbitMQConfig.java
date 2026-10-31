package com.example.paymentservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue paymentOrderCreatedQueue() {
        return new Queue("payment.order-created.queue", true);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange", true, false);
    }

    @Value("${app.messaging.payment-succeeded-queue}")
    private String paymentSucceededQueue;

    @Value("${app.messaging.payment-review-queue}")
    private String paymentReviewQueue;

    @Bean
    public Binding paymentOrderCreatedBinding() {
        return BindingBuilder
                .bind(paymentOrderCreatedQueue())
                .to(orderExchange())
                .with("order.created");
    }

    @Bean
    public Queue paymentSucceededQueue() {
        return new Queue(paymentSucceededQueue, true);
    }

    @Bean
    public Queue paymentReviewQueue() {
        return new Queue(paymentReviewQueue, true);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
