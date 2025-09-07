package com.project.product_service.api.dto.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductEditedEvent(
        Long productId,
        String name,
        String description,
        BigDecimal price,
        List<String> tags,
        Long eventId,
        Instant occurredAt
) {}