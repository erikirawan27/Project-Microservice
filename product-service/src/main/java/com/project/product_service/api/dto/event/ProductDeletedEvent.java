package com.project.product_service.api.dto.event;

import java.time.Instant;

public record ProductDeletedEvent(
        Long productId,
        Long eventId,
        Instant occurredAt
) {}
