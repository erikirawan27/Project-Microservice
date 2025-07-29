package com.project.product_service.service;

import com.project.product_service.dto.ProductCreatedEvent;
import com.project.product_service.messaging.publisher.ProductEventPublisher;
import com.project.product_service.model.Product;
import com.project.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    final ProductRepository productRepository;
    final ProductEventPublisher productEventPublisher;

    public ProductService(ProductRepository productRepository, ProductEventPublisher productEventPublisher) {
        this.productRepository = productRepository;
        this.productEventPublisher = productEventPublisher;
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
    }

    public Product createProduct(Product product) {
        Product saved = productRepository.save(product);
        List<String> tags = List.of("new");

        ProductCreatedEvent event = new ProductCreatedEvent(
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                tags
        );
        productEventPublisher.notifyProductCreated(event);
        return product;
    }

    public Product editProduct(Long id, Product product) {
        Product productOld = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));

        productOld.setName(product.getName());
        productOld.setDescription(product.getDescription());
        productOld.setPrice(product.getPrice());
        productOld.setStockQuantity(product.getStockQuantity());

        return productRepository.save(productOld);
    }

    public void deleteProduct(Long id) {
        Product productOld = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));

        productRepository.deleteById(id);
    }

}
