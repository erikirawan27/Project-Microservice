package com.project.product_service.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.product_service.api.dto.event.ProductCreatedEvent;
import com.project.product_service.api.dto.event.ProductDeletedEvent;
import com.project.product_service.api.dto.event.ProductEditedEvent;
import com.project.product_service.api.dto.request.ProductRequest;
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
import java.util.ArrayList;
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
    public List<Product> createProductBulk(List<ProductRequest> requestBulk) {
        List<Product> savedProducts = new ArrayList<>();
        for (ProductRequest req : requestBulk) {
            Product product = new Product();
            product.setId(req.getId());
            mapToEntity(product, req);
            Product saved = productRepository.save(product);
            savedProducts.add(saved);

            OutboxEvent outbox = new OutboxEvent();
            outbox.setAggregateId(saved.getId().toString());
            outbox.setEventType("PRODUCT_CREATED");
            outbox.setProcessed(false);
            outbox.setRetryCount(0);
            outboxRepository.save(outbox);

            List<String> tags = req.getTags();
            ProductCreatedEvent event = new ProductCreatedEvent(
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
        }
        return savedProducts;
    }

    @Transactional
    public Product editProduct(Long id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));

        mapToEntity(product, req);

        OutboxEvent outbox = new OutboxEvent();
        outbox.setAggregateId(product.getId().toString());
        outbox.setEventType("PRODUCT_EDITED");
        outbox.setProcessed(false);
        outbox.setRetryCount(0);
        outboxRepository.save(outbox);

        List<String> tags = req.getTags();
        ProductEditedEvent event = new ProductEditedEvent(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
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

        return productRepository.save(product);
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

    private void mapToEntity(Product product, ProductRequest req) {
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setDescription(req.getDescription());
        product.setStockQuantity(req.getStockQuantity());
    }

}
