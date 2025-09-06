package com.project.catalog_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductEvent(Long id, String name, String description, BigDecimal price, List<String> tags) {}

