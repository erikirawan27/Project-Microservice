package com.project.catalog_service.infra.db;

import com.project.catalog_service.domain.CatalogProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogProductRepository extends ElasticsearchRepository<CatalogProduct, Long> {
    List<CatalogProduct> findByNameContainingIgnoreCase(String name);
}
