package com.project.catalog_service.controller;

import com.project.catalog_service.model.CatalogProduct;
import com.project.catalog_service.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public Iterable<CatalogProduct> getAllProduct() {
        return catalogService.getAllProduct();
    }

    @GetMapping("/{name}")
    public List<CatalogProduct> findByNameContainingIgnoreCase(@PathVariable String name) {
        return catalogService.findByNameContainingIgnoreCase(name);
    }

    @PostMapping
    public CatalogProduct createCatalog(@RequestBody CatalogProduct catalogProduct) {
        return catalogService.createProduct(catalogProduct);
    }

    @DeleteMapping("/{id}")
    public void deleteCatalog(@PathVariable String id) {
        catalogService.deleteProduct(id);
    }
}
