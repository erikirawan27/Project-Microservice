package com.project.product_service.infra.messaging.publisher;

import com.project.product_service.api.dto.event.ProductCreatedEvent;
import com.project.product_service.api.dto.event.ProductDeletedEvent;
import com.project.product_service.api.dto.event.ProductEditedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.project.product_service.infra.messaging.config.RabbitMQConstants.*;

@Service
public class ProductEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public ProductEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void notifyProductCreated(ProductCreatedEvent event) {
        LOGGER.info("ProductEvent sent: {}", event.toString());
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_CREATED, event, message -> {
            message.getMessageProperties().setMessageId("product-created-"+event.eventId());
            return message;
        });
    }

    public void notifyProductEdited(ProductEditedEvent event) {
        LOGGER.info("notifyProductEdited sent: {}", event.toString());
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_EDITED, event, message -> {
            message.getMessageProperties().setMessageId("product-edited-"+event.eventId());
            return message;
        });
    }

    public void notifyProductDeleted(ProductDeletedEvent event) {
        LOGGER.info("notifyProductDeleted sent: {}", event.toString());
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_DELETED, event, message -> {
            message.getMessageProperties().setMessageId("product-deleted-"+event.eventId());
            return message;
        });
    }

}
