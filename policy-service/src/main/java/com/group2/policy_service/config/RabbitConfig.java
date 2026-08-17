package com.group2.policy_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "policy.exchange";
    public static final String PURCHASE_QUEUE = "policy.purchase.queue";
    public static final String PURCHASE_ROUTING_KEY = "policy.purchase.started";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue purchaseQueue() {
        return new Queue(PURCHASE_QUEUE);
    }

    @Bean
    public Binding purchaseBinding(@Qualifier("purchaseQueue") Queue purchaseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(purchaseQueue).to(exchange).with(PURCHASE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
