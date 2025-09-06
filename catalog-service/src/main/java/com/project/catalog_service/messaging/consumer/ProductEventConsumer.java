package com.project.catalog_service.messaging.consumer;

import com.project.catalog_service.dto.ProductEvent;
import com.project.catalog_service.model.CatalogProduct;
import com.project.catalog_service.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.project.catalog_service.messaging.config.RabbitMQConstants.*;

@Service
public class ProductEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventConsumer.class);
    private final CatalogService catalogService;

    public ProductEventConsumer(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @RabbitListener(queues = PRODUCT_CREATED_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleProductCreated(ProductEvent productDto) {
        LOGGER.info("ProductCreatedEvent received: {}", productDto);
        CatalogProduct product = map(productDto);

        catalogService.createIfAbsent(product);
    }

    @RabbitListener(queues = PRODUCT_EDITED_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleProductEdited(ProductEvent productDto) {
        LOGGER.info("ProductEditedEvent received: {}", productDto);
        CatalogProduct product = map(productDto);

        catalogService.replaceByDeleteInsert(product);
    }

    private CatalogProduct  map(ProductEvent productEvent) {
        CatalogProduct p = new CatalogProduct();
        p.setId(productEvent.id());
        p.setName(productEvent.name());
        p.setDescription(productEvent.description());
        p.setPrice(productEvent.price());
        p.setTags(productEvent.tags());
        return p;
    }
}
