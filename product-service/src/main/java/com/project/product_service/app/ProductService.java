package com.project.product_service.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.product_service.api.dto.event.ProductCreatedEven;
import com.project.product_service.api.dto.event.ProductDeletedEvent;
import com.project.product_service.api.dto.event.ProductEditedEvent;
import com.project.product_service.domain.OutboxEvent;
import com.project.product_service.infra.db.OutboxEventRepository;
import com.project.product_service.infra.messaging.publisher.ProductEventPublisher;
import com.project.product_service.domain.Product;
import com.project.product_service.infra.db.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    final ProductRepository productRepository;
    final ProductEventPublisher productEventPublisher;
    final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, ProductEventPublisher productEventPublisher, OutboxEventRepository outboxRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.productEventPublisher = productEventPublisher;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
    }

    @Transactional
    public Product createProduct(Product product) {
        Product saved = productRepository.save(product);

        OutboxEvent outbox = new OutboxEvent();
        outbox.setAggregateId(saved.getId().toString());
        outbox.setEventType("PRODUCT_CREATED");
        outbox.setProcessed(false);
        outbox.setRetryCount(0);
        outboxRepository.save(outbox);

        List<String> tags = List.of("new");
        ProductCreatedEven event = new ProductCreatedEven(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                tags,
                outbox.getId(),
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            outbox.setPayload(payload);

            try {
                productEventPublisher.notifyProductCreated(event);
                outbox.setProcessed(true);
            } catch (Exception mqEx) {
                LOGGER.warn("RabbitMQ publish failed, will retry later: {}", mqEx.getMessage());
            }

            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
        return saved;
    }

    @Transactional
    public Product editProduct(Long id, Product product) {
        Product productOld = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));

        List<String> tags = List.of("new");
        productOld.setName(product.getName());
        productOld.setDescription(product.getDescription());
        productOld.setPrice(product.getPrice());
        productOld.setStockQuantity(product.getStockQuantity());

        OutboxEvent outbox = new OutboxEvent();
        outbox.setAggregateId(productOld.getId().toString());
        outbox.setEventType("PRODUCT_EDITED");
        outbox.setProcessed(false);
        outbox.setRetryCount(0);
        outboxRepository.save(outbox);

        ProductEditedEvent event = new ProductEditedEvent(
                productOld.getId(),
                productOld.getName(),
                productOld.getDescription(),
                productOld.getPrice(),
                tags,
                outbox.getId(),
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            outbox.setPayload(payload);

            try {
                productEventPublisher.notifyProductEdited(event);
                outbox.setProcessed(true);
            } catch (Exception mqEx) {
                LOGGER.warn("RabbitMQ publish failed, will retry later: {}", mqEx.getMessage());
            }

            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        return productRepository.save(productOld);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
        if (product != null) {

            OutboxEvent outbox = new OutboxEvent();
            outbox.setAggregateId(id.toString());
            outbox.setEventType("PRODUCT_DELETED");
            outbox.setProcessed(false);
            outbox.setRetryCount(0);
            outboxRepository.save(outbox);

            ProductDeletedEvent event = new ProductDeletedEvent(id,outbox.getId(), Instant.now());

            try {
                String payload = objectMapper.writeValueAsString(event);
                outbox.setPayload(payload);

                productRepository.deleteById(id);
                try {
                    productEventPublisher.notifyProductDeleted(event);
                    outbox.setProcessed(true);
                } catch (Exception mqEx) {
                    LOGGER.warn("RabbitMQ publish failed, will retry later: {}", mqEx.getMessage());
                }
            }  catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize event", e);
            }
            outboxRepository.save(outbox);
        }
    }

}
