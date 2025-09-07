package com.project.catalog_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(indexName = "products")
public class CatalogProduct {
    @Id
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private List<String> tags;

    @Field(name = "indexed_at", type = FieldType.Date)
    private Instant indexedAt = Instant.now();
}
