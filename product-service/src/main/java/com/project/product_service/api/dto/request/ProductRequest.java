package com.project.product_service.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class ProductRequest {
    Long id;
    String name;
    String description;
    BigDecimal price;
    Integer stockQuantity;
    List<String> tags;
}
