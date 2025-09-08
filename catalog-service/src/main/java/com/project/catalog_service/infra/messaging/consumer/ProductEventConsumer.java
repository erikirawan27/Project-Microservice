package com.project.catalog_service.infra.messaging.consumer;

import com.project.catalog_service.api.dto.event.ProductCreatedEvent;
import com.project.catalog_service.api.dto.event.ProductDeletedEvent;
import com.project.catalog_service.api.dto.event.ProductEditedEvent;
import com.project.catalog_service.domain.CatalogProduct;
import com.project.catalog_service.app.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.project.catalog_service.infra.messaging.config.RabbitMQConstants.*;

@Service
public class ProductEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventConsumer.class);
    private final CatalogService catalogService;

    public ProductEventConsumer(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @RabbitListener(queues = PRODUCT_CREATED_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleProductCreated(ProductCreatedEvent event) {
        LOGGER.info("ProductCreatedEvent received: {}", event);

        CatalogProduct product = new CatalogProduct();
        product.setId(event.productId());
        product.setName(event.name());
        product.setDescription(event.description());
        product.setPrice(event.price());
        product.setTags(event.tags());

        catalogService.createIfAbsent(product);
    }

    @RabbitListener(queues = PRODUCT_EDITED_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleProductEdited(ProductEditedEvent event) {
        LOGGER.info("ProductEditedEvent received: {}", event);

        CatalogProduct product = new CatalogProduct();
        product.setId(event.productId());
        product.setName(event.name());
        product.setDescription(event.description());
        product.setPrice(event.price());
        product.setTags(event.tags());

        catalogService.replaceByDeleteInsert(product);
    }

    @RabbitListener(queues = PRODUCT_DELETED_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleProductDeleted(ProductDeletedEvent event) {
        LOGGER.info("ProductDeletedEvent received: {}", event);

        Long productID = event.productId();
        catalogService.deleteProduct(productID);
    }

}
