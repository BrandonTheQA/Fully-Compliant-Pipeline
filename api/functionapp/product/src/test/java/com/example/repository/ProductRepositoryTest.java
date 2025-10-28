package com.example.repository;

import com.example.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductRepository
 */
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
    }

    @Test
    @DisplayName("Should save product successfully")
    void shouldSaveProductSuccessfully() {
        // Given
        Product product = new Product("product-id", "Test Product", "Test Description", 29.99, 100, "Electronics");
        product.setCreatedAt("2023-01-01T00:00:00");
        product.setUpdatedAt("2023-01-01T00:00:00");

        // When
        Product savedProduct = productRepository.save(product);

        // Then
        assertNotNull(savedProduct);
        assertEquals("product-id", savedProduct.getId());
        assertEquals("Test Product", savedProduct.getName());
        assertEquals("Test Description", savedProduct.getDescription());
        assertEquals(29.99, savedProduct.getPrice());
        assertEquals(100, savedProduct.getQuantity());
        assertEquals("Electronics", savedProduct.getCategory());
        assertNotNull(savedProduct.getCreatedAt());
    }

    @Test
    @DisplayName("Should find product by id")
    void shouldFindProductById() {
        // Given
        Product product = new Product("product-id", "Test Product", "Test Description", 29.99, 100, "Electronics");
        productRepository.save(product);

        // When
        Optional<Product> foundProduct = productRepository.findById("product-id");

        // Then
        assertTrue(foundProduct.isPresent());
        assertEquals("product-id", foundProduct.get().getId());
        assertEquals("Test Product", foundProduct.get().getName());
        assertEquals(29.99, foundProduct.get().getPrice());
    }

    @Test
    @DisplayName("Should return empty when product not found by id")
    void shouldReturnEmptyWhenProductNotFoundById() {
        // When
        Optional<Product> foundProduct = productRepository.findById("non-existent-id");

        // Then
        assertFalse(foundProduct.isPresent());
    }

    @Test
    @DisplayName("Should find all products")
    void shouldFindAllProducts() {
        // Given
        Product product1 = new Product("product-1", "Product 1", "Description 1", 10.0, 50, "Category 1");
        Product product2 = new Product("product-2", "Product 2", "Description 2", 20.0, 75, "Category 2");
        
        productRepository.save(product1);
        productRepository.save(product2);

        // When
        List<Product> products = productRepository.findAll();

        // Then
        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> p.getId().equals("product-1")));
        assertTrue(products.stream().anyMatch(p -> p.getId().equals("product-2")));
    }

    @Test
    @DisplayName("Should check if product exists by id")
    void shouldCheckIfProductExistsById() {
        // Given
        Product product = new Product("product-id", "Test Product", "Test Description", 29.99, 100, "Electronics");
        productRepository.save(product);

        // When & Then
        assertTrue(productRepository.existsById("product-id"));
        assertFalse(productRepository.existsById("non-existent-id"));
    }

    @Test
    @DisplayName("Should check if product exists by name")
    void shouldCheckIfProductExistsByName() {
        // Given
        Product product = new Product("product-id", "Test Product", "Test Description", 29.99, 100, "Electronics");
        productRepository.save(product);

        // When & Then
        assertTrue(productRepository.existsByName("Test Product"));
        assertFalse(productRepository.existsByName("Non-existent Product"));
    }

    @Test
    @DisplayName("Should count products correctly")
    void shouldCountProductsCorrectly() {
        // Given
        assertEquals(0, productRepository.count());

        // When
        Product product1 = new Product("product-1", "Product 1", "Description 1", 10.0, 50, "Category 1");
        Product product2 = new Product("product-2", "Product 2", "Description 2", 20.0, 75, "Category 2");
        
        productRepository.save(product1);
        assertEquals(1, productRepository.count());
        
        productRepository.save(product2);
        assertEquals(2, productRepository.count());
    }

    @Test
    @DisplayName("Should handle multiple products independently")
    void shouldHandleMultipleProductsIndependently() {
        // Given
        Product product1 = new Product("product-1", "Product 1", "Description 1", 10.0, 50, "Category 1");
        Product product2 = new Product("product-2", "Product 2", "Description 2", 20.0, 75, "Category 2");
        
        productRepository.save(product1);
        productRepository.save(product2);

        // When
        Optional<Product> foundProduct1 = productRepository.findById("product-1");
        Optional<Product> foundProduct2 = productRepository.findById("product-2");

        // Then
        assertTrue(foundProduct1.isPresent());
        assertTrue(foundProduct2.isPresent());
        
        assertEquals("Product 1", foundProduct1.get().getName());
        assertEquals("Product 2", foundProduct2.get().getName());
        assertEquals(10.0, foundProduct1.get().getPrice());
        assertEquals(20.0, foundProduct2.get().getPrice());
        
        assertEquals(2, productRepository.count());
    }

    @Test
    @DisplayName("Should update existing product")
    void shouldUpdateExistingProduct() {
        // Given
        Product originalProduct = new Product("product-id", "Original Product", "Original Description", 29.99, 100, "Electronics");
        productRepository.save(originalProduct);

        // When
        Product updatedProduct = new Product("product-id", "Updated Product", "Updated Description", 39.99, 150, "Updated Category");
        productRepository.save(updatedProduct);

        // Then
        Optional<Product> foundProduct = productRepository.findById("product-id");
        assertTrue(foundProduct.isPresent());
        assertEquals("Updated Product", foundProduct.get().getName());
        assertEquals("Updated Description", foundProduct.get().getDescription());
        assertEquals(39.99, foundProduct.get().getPrice());
        assertEquals(150, foundProduct.get().getQuantity());
        assertEquals("Updated Category", foundProduct.get().getCategory());
        assertEquals(1, productRepository.count()); // Should still be 1 product, not 2
    }

    @Test
    @DisplayName("Should delete product by id")
    void shouldDeleteProductById() {
        // Given
        Product product = new Product("product-id", "Test Product", "Test Description", 29.99, 100, "Electronics");
        productRepository.save(product);
        assertEquals(1, productRepository.count());

        // When
        productRepository.deleteById("product-id");

        // Then
        assertEquals(0, productRepository.count());
        assertFalse(productRepository.findById("product-id").isPresent());
    }

    @Test
    @DisplayName("Should handle null product gracefully")
    void shouldHandleNullProductGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> productRepository.save(null));
    }
}

