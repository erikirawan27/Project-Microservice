package com.project.catalog_service.messaging.consumer;

import com.project.catalog_service.dto.ProductCreatedEvent;
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

    @RabbitListener(queues = PRODUCT_CREATED_QUEUE)
    public void handleProductCreated(ProductCreatedEvent productDto) {
        LOGGER.info("ProductCreatedEvent sent: {}", productDto);
        CatalogProduct product = new CatalogProduct();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setTags(productDto.getTags());
        catalogService.createProduct(product);
    }
}
