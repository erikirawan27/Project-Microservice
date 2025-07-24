package com.project.product_service.service;

import com.project.product_service.model.Product;
import com.project.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    final
    ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
    }

    public Product crateProduct(Product product) {
        return productRepository.save(product);
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
