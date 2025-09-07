package com.project.catalog_service.app;

import com.project.catalog_service.domain.CatalogProduct;
import com.project.catalog_service.infra.db.CatalogProductRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    final CatalogProductRepository catalogProductRepository;
    final ElasticsearchOperations esOps;

    public CatalogService(CatalogProductRepository catalogProductRepository, ElasticsearchOperations esOps) {
        this.catalogProductRepository = catalogProductRepository;
        this.esOps = esOps;
    }

    public Iterable<CatalogProduct> getAllProduct() {
        return catalogProductRepository.findAll();
    }

    public List<CatalogProduct> findByNameContainingIgnoreCase(String name) {
        return catalogProductRepository.findByNameContainingIgnoreCase(name);
    }

    public void deleteProduct(Long id) {
        catalogProductRepository.deleteById(id);
        esOps.indexOps(CatalogProduct.class).refresh();
    }

    public void createIfAbsent(CatalogProduct data) {
        if (!catalogProductRepository.existsById(data.getId())) {
            catalogProductRepository.save(data);
        }
    }

    public void replaceByDeleteInsert(CatalogProduct data) {
        try {
            catalogProductRepository.deleteById(data.getId()); // ignore if missing
        } catch (Exception ignored) { }
        catalogProductRepository.save(data);
        esOps.indexOps(CatalogProduct.class).refresh();
    }

}
