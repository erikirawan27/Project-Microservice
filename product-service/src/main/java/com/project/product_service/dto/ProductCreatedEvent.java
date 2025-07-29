package com.project.product_service.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent implements Serializable {
    private String name;
    private String description;
    private BigDecimal price;
    private List<String> tags;

}
