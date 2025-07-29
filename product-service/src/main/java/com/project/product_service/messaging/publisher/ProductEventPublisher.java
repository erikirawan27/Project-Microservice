package com.project.product_service.messaging.publisher;

import com.project.product_service.dto.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.project.product_service.messaging.config.RabbitMQConstants.*;

@Service
public class ProductEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public ProductEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void notifyProductCreated(ProductCreatedEvent productDto) {
        LOGGER.info("ProductCreatedEvent sent: {}", productDto.toString());
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_CREATED, productDto);
    }
}
