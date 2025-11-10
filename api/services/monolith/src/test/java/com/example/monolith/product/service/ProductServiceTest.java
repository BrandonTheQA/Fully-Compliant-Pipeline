package com.example.monolith.product.service;

import com.example.monolith.product.dto.CreateProductRequest;
import com.example.monolith.product.dto.ProductResponse;
import com.example.monolith.product.exception.ProductNotFoundException;
import com.example.monolith.product.model.Product;
import com.example.monolith.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 */
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("Should get all products successfully")
    void shouldGetAllProductsSuccessfully() {
        // Given
        Product product1 = new Product("product-1", "Product 1", "Description 1", 10.0, 50, "Category 1");
        Product product2 = new Product("product-2", "Product 2", "Description 2", 20.0, 75, "Category 2");
        List<Product> products = Arrays.asList(product1, product2);
        
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductResponse> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("product-1", result.get(0).getId());
        assertEquals("product-2", result.get(1).getId());
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void shouldReturnEmptyListWhenNoProductsExist() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<ProductResponse> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should get product successfully")
    void shouldGetProductSuccessfully() {
        // Given
        String productId = "product-id";
        Product product = new Product(productId, "Test Product", "Test Description", 29.99, 100, "Electronics");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductResponse result = productService.getProduct(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(29.99, result.getPrice());
        assertEquals(100, result.getQuantity());
        assertEquals("Electronics", result.getCategory());
        assertNotNull(result.getCreatedAt());
        
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found")
    void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
        // Given
        String productId = "non-existent-id";
        
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(productId));
        
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should create new product successfully")
    void shouldCreateNewProductSuccessfully() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                "New Product", "New Description", 39.99, 50, "Electronics");
        Product savedProduct = new Product("product-id", "New Product", "New Description", 39.99, 50, "Electronics");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductResponse result = productService.createOrUpdateProduct(request);

        // Then
        assertNotNull(result);
        assertEquals("product-id", result.getId());
        assertEquals("New Product", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals(39.99, result.getPrice());
        assertEquals(50, result.getQuantity());
        assertEquals("Electronics", result.getCategory());
        assertNotNull(result.getCreatedAt());
        
        verify(productRepository).findAll();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update existing product successfully")
    void shouldUpdateExistingProductSuccessfully() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                "Existing Product", "Updated Description", 49.99, 75, "Updated Category");
        Product existingProduct = new Product("product-id", "Existing Product", "Old Description", 29.99, 50, "Old Category");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // When
        ProductResponse result = productService.createOrUpdateProduct(request);

        // Then
        assertNotNull(result);
        assertEquals("product-id", result.getId());
        assertEquals("Existing Product", result.getName());
        // Verify the product was updated
        verify(productRepository).save(any(Product.class));
        
        // Verify updated fields
        assertEquals("Updated Description", existingProduct.getDescription());
        assertEquals(49.99, existingProduct.getPrice());
        assertEquals(75, existingProduct.getQuantity());
        assertEquals("Updated Category", existingProduct.getCategory());
    }

    @Test
    @DisplayName("Should map product to response correctly")
    void shouldMapProductToResponseCorrectly() {
        // Given
        Product product = new Product("product-id", "Test Product", "Test Description", 29.99, 100, "Electronics");

        // When - Test the mapping indirectly through getProduct
        when(productRepository.findById("product-id")).thenReturn(Optional.of(product));
        ProductResponse result = productService.getProduct("product-id");

        // Then
        assertNotNull(result);
        assertEquals("product-id", result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(29.99, result.getPrice());
        assertEquals(100, result.getQuantity());
        assertEquals("Electronics", result.getCategory());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void shouldHandleNullInputGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> productService.createOrUpdateProduct(null));
        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(null));
    }
}

