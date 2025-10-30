package com.example.service;

import com.example.dto.CreateProductRequest;
import com.example.dto.ProductResponse;
import com.example.exception.ProductNotFoundException;
import com.example.model.Product;
import com.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProductResponse getProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    public ProductResponse createOrUpdateProduct(CreateProductRequest request) {
        List<Product> existingProducts = productRepository.findAll();
        Product existingProduct = existingProducts.stream()
                .filter(p -> p.getName().equals(request.getName()))
                .findFirst()
                .orElse(null);

        Product product;
        if (existingProduct != null) {
            existingProduct.setDescription(request.getDescription());
            existingProduct.setPrice(request.getPrice());
            existingProduct.setQuantity(request.getQuantity());
            existingProduct.setCategory(request.getCategory());
            existingProduct.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            product = productRepository.save(existingProduct);
        } else {
            String productId = UUID.randomUUID().toString();
            product = new Product(
                    productId,
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getQuantity(),
                    request.getCategory()
            );
            product = productRepository.save(product);
        }
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}


