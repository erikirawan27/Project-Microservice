package com.project.product_service.infra.messaging.config;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.project.product_service.infra.messaging.config.RabbitMQConstants.*;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange productExchange() {
        return new DirectExchange(PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // Quese(queue, durable,exclusive,autoDelete)
    @Bean
    public Queue productCreatedQueue() {
        return new Queue(PRODUCT_CREATED_QUEUE, true, false, false, Map.of(
                "x-dead-letter-exchange", DLX_EXCHANGE,
                "x-dead-letter-routing-key", PRODUCT_CREATED + ".dlq"
        ));
    }

    @Bean
    public Queue productEditedQueue() {
        return new Queue(PRODUCT_EDITED_QUEUE, true, false, false, Map.of(
                "x-dead-letter-exchange", DLX_EXCHANGE,
                "x-dead-letter-routing-key", PRODUCT_EDITED + ".dlq"
        ));
    }

    @Bean
    public Queue productDeletedQueue() {
        return new Queue(PRODUCT_DELETED_QUEUE, true, false, false, Map.of(
                "x-dead-letter-exchange", DLX_EXCHANGE,
                "x-dead-letter-routing-key", PRODUCT_EDITED + ".dlq"
        ));
    }

    @Bean
    public Binding productCreatedBinding() {
        return BindingBuilder
                .bind(productCreatedQueue())
                .to(productExchange())
                .with(PRODUCT_CREATED);
    }

    @Bean
    public Binding productEditedBinding() {
        return BindingBuilder
                .bind(productEditedQueue())
                .to(productExchange())
                .with(PRODUCT_EDITED);
    }

    @Bean
    public Binding productDeletedBinding() {
        return BindingBuilder
                .bind(productDeletedQueue())
                .to(productExchange())
                .with(PRODUCT_DELETED);
    }

    //DLQ to store failed message
    @Bean Queue productCreatedDlq() { return new Queue(PRODUCT_CREATED_QUEUE + ".dlq", true); }
    @Bean Queue productEditedDlq()  { return new Queue(PRODUCT_EDITED_QUEUE  + ".dlq", true); }
    @Bean Queue productDeletedDlq()  { return new Queue(PRODUCT_DELETED_QUEUE  + ".dlq", true); }

    @Bean
    public Binding productCreatedDlqBinding() {
        return BindingBuilder.bind(productCreatedDlq()).to(deadLetterExchange()).with(PRODUCT_CREATED + ".dlq");
    }

    @Bean
    public Binding productEditedDlqBinding() {
        return BindingBuilder.bind(productEditedDlq()).to(deadLetterExchange()).with(PRODUCT_EDITED + ".dlq");
    }
    @Bean
    public Binding productDeletedDlqBinding() {
        return BindingBuilder.bind(productEditedDlq()).to(deadLetterExchange()).with(PRODUCT_DELETED + ".dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter mc) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(connectionFactory);
        f.setMessageConverter(mc);
        f.setPrefetchCount(20);
        f.setConcurrentConsumers(2);
        f.setMaxConcurrentConsumers(8);
        f.setDefaultRequeueRejected(false); // handle requeue

        //Retry before go to dlq
        f.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 10000) // initial, multiplier, max
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return f;
    }
}
