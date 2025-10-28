package com.example.controller;

import com.example.dto.CreateProductRequest;
import com.example.dto.ProductResponse;
import com.example.exception.ProductNotFoundException;
import com.example.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ProductController
 */
@DisplayName("ProductController Tests")
class ProductControllerTest {

    private MockMvc mockMvc;
    private ProductService productService;
    private ObjectMapper objectMapper;
    private CreateProductRequest createProductRequest;
    private ProductResponse productResponse;
    private List<ProductResponse> productList;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        ProductController productController = new ProductController();
        // Use reflection to inject the service
        try {
            java.lang.reflect.Field field = ProductController.class.getDeclaredField("productService");
            field.setAccessible(true);
            field.set(productController, productService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject service", e);
        }
        
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new com.example.exception.GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
        
        // Setup test data
        createProductRequest = new CreateProductRequest("Test Product", "Test Description", 29.99, 100, "Electronics");
        productResponse = new ProductResponse("product-id", "Test Product", "Test Description", 29.99, 100, "Electronics", "2023-01-01T00:00:00", "2023-01-01T00:00:00");
        productList = Arrays.asList(productResponse);
    }

    @Test
    @DisplayName("Should get all products successfully")
    void shouldGetAllProductsSuccessfully() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("product-id"))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].description").value("Test Description"))
                .andExpect(jsonPath("$[0].price").value(29.99))
                .andExpect(jsonPath("$[0].quantity").value(100))
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("Should get product successfully")
    void shouldGetProductSuccessfully() throws Exception {
        // Given
        when(productService.getProduct("product-id")).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/product-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-id"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.createdAt").value("2023-01-01T00:00:00"));

        verify(productService).getProduct("product-id");
    }

    @Test
    @DisplayName("Should handle ProductNotFoundException")
    void shouldHandleProductNotFoundException() throws Exception {
        when(productService.getProduct("non-existent-id"))
                .thenThrow(new ProductNotFoundException("Product not found"));
        mockMvc.perform(get("/api/products/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"))
                .andExpect(jsonPath("$.message").value("Product not found"));
        verify(productService).getProduct("non-existent-id");
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        when(productService.createOrUpdateProduct(any(CreateProductRequest.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("product-id"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.category").value("Electronics"));

        verify(productService).createOrUpdateProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for create product")
    void shouldHandleValidationErrorsForCreateProduct() throws Exception {
        // Given - invalid request with missing required fields
        CreateProductRequest invalidRequest = new CreateProductRequest("", null, -10.0, -5, "");

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createOrUpdateProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isInternalServerError());

        verify(productService, never()).createOrUpdateProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isInternalServerError());

        verify(productService, never()).createOrUpdateProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Should handle null path variable")
    void shouldHandleNullPathVariable() throws Exception {
        // Given
        when(productService.getProduct("null")).thenThrow(new ProductNotFoundException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/products/null"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));

        verify(productService).getProduct("null");
    }

    @Test
    @DisplayName("Should handle unsupported media type")
    void shouldHandleUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isInternalServerError());

        verify(productService, never()).createOrUpdateProduct(any(CreateProductRequest.class));
    }
}

