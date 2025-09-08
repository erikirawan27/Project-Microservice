package com.project.product_service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.product_service.api.dto.event.ProductCreatedEvent;
import com.project.product_service.api.dto.event.ProductDeletedEvent;
import com.project.product_service.api.dto.event.ProductEditedEvent;
import com.project.product_service.domain.OutboxEvent;
import com.project.product_service.infra.db.OutboxEventRepository;
import com.project.product_service.infra.messaging.publisher.ProductEventPublisher;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class OutboxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxService.class);
    private final OutboxEventRepository outboxRepository;
    final ProductEventPublisher productEventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxRepository, ProductEventPublisher productEventPublisher, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.productEventPublisher = productEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000) //5s
    @Transactional
    public void publishEvent() {
        List<OutboxEvent> outboxEvents = outboxRepository.findTop10ByProcessedFalseAndRetryCountLessThanOrderByCreatedAtAsc(5);
        for (OutboxEvent outbox : outboxEvents) {
            try {
                switch (outbox.getEventType()) {
                    case "PRODUCT_CREATED" :
                        ProductCreatedEvent eventCreated = objectMapper.readValue(outbox.getPayload(), ProductCreatedEvent.class);
                        productEventPublisher.notifyProductCreated(eventCreated);
                        break;
                    case "PRODUCT_EDITED" :
                        ProductEditedEvent eventEdited = objectMapper.readValue(outbox.getPayload(), ProductEditedEvent.class);
                        productEventPublisher.notifyProductEdited(eventEdited);
                        break;
                    case "PRODUCT_DELETED" :
                        ProductDeletedEvent eventDeleted = objectMapper.readValue(outbox.getPayload(), ProductDeletedEvent.class);
                        productEventPublisher.notifyProductDeleted(eventDeleted);
                        break;
                }
                outbox.setProcessed(true);
                outboxRepository.save(outbox);
            } catch (Exception e) {
                LOGGER.error("Error sending message: {}", e.getMessage(), e);
                int retries = outbox.getRetryCount() + 1;
                outbox.setRetryCount(retries);
                outboxRepository.save(outbox);
            }
        }
    }
}
