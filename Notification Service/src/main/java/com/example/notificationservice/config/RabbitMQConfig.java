package com.example.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final String DEAD_LETTER_EXCHANGE = "notification.dead-letter.exchange";
    private static final String DEAD_LETTER_ROUTING_KEY = "notification.dead-letter";

    @Value("${app.messaging.retry.max-attempts}")
    private int retryMaxAttempts;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            RabbitTemplate rabbitTemplate) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(retryMaxAttempts)
                .backOffOptions(1_000, 2.0, 10_000)
                .recoverer(new RepublishMessageRecoverer(
                        rabbitTemplate,
                        DEAD_LETTER_EXCHANGE,
                        DEAD_LETTER_ROUTING_KEY
                ))
                .build());
        return factory;
    }

    @Bean
    public Queue notificationOrderCreatedQueue() {
        return new Queue("notification.order-created.queue", true);
    }

    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return new Queue("notification.dead-letter.queue", true);
    }

    @Bean
    public Binding notificationDeadLetterBinding() {
        return BindingBuilder
                .bind(notificationDeadLetterQueue())
                .to(notificationDeadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange", true, false);
    }

    @Value("${app.messaging.order-update-status-queue}")
    private String orderUpdateStatusQueue;

    @Value("${app.messaging.payment-review-queue}")
    private String paymentReviewQueue;

    @Value("${app.messaging.otp-queue}")
    private String otpQueue;

    @Bean
    public Binding notificationOrderCreatedBinding() {
        return BindingBuilder
                .bind(notificationOrderCreatedQueue())
                .to(orderExchange())
                .with("order.created");
    }

    @Bean
    public Queue orderUpdateStatusQueue() {
        return new Queue(orderUpdateStatusQueue, true);
    }


    @Bean
    public Queue paymentReviewQueue() {
        return new Queue(paymentReviewQueue, true);
    }

    @Bean
    public Queue otpQueue() {
        return new Queue(otpQueue, true);
    }

    @Bean("jacksonMessageConverter")
    public Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}