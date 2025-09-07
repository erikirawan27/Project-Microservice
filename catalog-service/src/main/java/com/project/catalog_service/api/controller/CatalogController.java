package com.project.catalog_service.api.controller;

import com.project.catalog_service.domain.CatalogProduct;
import com.project.catalog_service.app.CatalogService;
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

}
