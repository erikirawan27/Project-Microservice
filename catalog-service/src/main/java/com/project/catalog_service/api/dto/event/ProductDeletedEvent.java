package com.project.catalog_service.api.dto.event;

import java.time.Instant;

public record ProductDeletedEvent(
        Long productId,
        Long eventId,
        Instant occurredAt
) {}
