package com.project.product_service.infra.db;

import com.project.product_service.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop10ByProcessedFalseAndRetryCountLessThanOrderByCreatedAtAsc(int retryCount);
}
