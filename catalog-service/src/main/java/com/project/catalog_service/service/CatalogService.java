package com.project.catalog_service.service;

import com.project.catalog_service.model.CatalogProduct;
import com.project.catalog_service.repository.CatalogProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    final CatalogProductRepository catalogProductRepository;

    public CatalogService(CatalogProductRepository catalogProductRepository) {
        this.catalogProductRepository = catalogProductRepository;
    }

    public Iterable<CatalogProduct> getAllProduct() {
        return catalogProductRepository.findAll();
    }

    public List<CatalogProduct> findByNameContainingIgnoreCase(String name) {
        return catalogProductRepository.findByNameContainingIgnoreCase(name);
    }

    public CatalogProduct createProduct(CatalogProduct catalogProduct) {
        return catalogProductRepository.save(catalogProduct);
    }

    public void deleteProduct(String id) {
        catalogProductRepository.deleteById(id);
    }
}
