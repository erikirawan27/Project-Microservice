package com.project.product_service.api.controller;

import com.project.product_service.api.dto.request.ProductRequest;
import com.project.product_service.app.ProductService;
import com.project.product_service.domain.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProduct() {
        return productService.getAllProduct();
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable long id) {
        return productService.getProductById(id);
    }

    @PostMapping()
    public ResponseEntity<List<Product>> createProductBulk(@RequestBody List<ProductRequest> requests) {
        List<Product> products = productService.createProductBulk(requests);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public Product editProduct(@PathVariable Long id, @RequestBody ProductRequest product) {
        return productService.editProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
