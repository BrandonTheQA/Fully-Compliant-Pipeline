package com.example.ecompoc.stock.controller;

import com.example.ecompoc.common.exception.GlobalExceptionHandler;
import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.dto.*;
import com.example.ecompoc.stock.model.StockNotification;
import com.example.ecompoc.stock.model.StockStatus;
import com.example.ecompoc.stock.repository.LowStockAlertRepository;
import com.example.ecompoc.stock.service.StockNotificationService;
import com.example.ecompoc.stock.service.StockStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("StockController Tests")
class StockControllerTest {
    
    private MockMvc mockMvc;
    private StockStatusService stockStatusService;
    private StockNotificationService notificationService;
    private ProductRepository productRepository;
    private LowStockAlertRepository alertRepository;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        stockStatusService = mock(StockStatusService.class);
        notificationService = mock(StockNotificationService.class);
        productRepository = mock(ProductRepository.class);
        alertRepository = mock(LowStockAlertRepository.class);
        
        StockController controller = new StockController(
            stockStatusService, notificationService, productRepository, alertRepository);
        
        // Enable feature toggle for tests
        ReflectionTestUtils.setField(controller, "stockManagementEnabled", true);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        
        objectMapper = new ObjectMapper();
    }
    
    @Test
    @DisplayName("Should get stock status successfully")
    void shouldGetStockStatusSuccessfully() throws Exception {
        // Given
        String productId = "product-1";
        Product product = new Product(productId, "Product 1", "Description", 10.0, 50, "Category");
        product.setLowStockThreshold(10);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockStatusService.calculateStockStatus(product)).thenReturn(StockStatus.IN_STOCK);
        when(stockStatusService.getStockStatusMessage(product)).thenReturn("In Stock");
        
        // When & Then
        mockMvc.perform(get("/api/v2/products/{id}/stock", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId))
            .andExpect(jsonPath("$.status").value("IN_STOCK"))
            .andExpect(jsonPath("$.quantity").value(50))
            .andExpect(jsonPath("$.message").value("In Stock"));
    }
    
    @Test
    @DisplayName("Should return 404 when product not found")
    void shouldReturn404WhenProductNotFound() throws Exception {
        // Given
        String productId = "non-existent";
        
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/v2/products/{id}/stock", productId))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should get bulk stock status successfully")
    void shouldGetBulkStockStatusSuccessfully() throws Exception {
        // Given
        BulkStockStatusRequest request = new BulkStockStatusRequest();
        request.setProductIds(Arrays.asList("product-1", "product-2"));
        
        Product product1 = new Product("product-1", "Product 1", "Description", 10.0, 50, "Category");
        Product product2 = new Product("product-2", "Product 2", "Description", 20.0, 5, "Category");
        
        when(productRepository.findAllById(request.getProductIds())).thenReturn(Arrays.asList(product1, product2));
        when(stockStatusService.getBulkStockStatus(request.getProductIds()))
            .thenReturn(java.util.Map.of("product-1", StockStatus.IN_STOCK, "product-2", StockStatus.LOW_STOCK));
        when(stockStatusService.getStockStatusMessage(product1)).thenReturn("In Stock");
        when(stockStatusService.getStockStatusMessage(product2)).thenReturn("Low Stock - Only 5 left!");
        
        // When & Then
        mockMvc.perform(post("/api/v2/products/stock/bulk")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statuses").isArray())
            .andExpect(jsonPath("$.statuses[0].productId").value("product-1"))
            .andExpect(jsonPath("$.statuses[0].status").value("IN_STOCK"))
            .andExpect(jsonPath("$.statuses[1].productId").value("product-2"))
            .andExpect(jsonPath("$.statuses[1].status").value("LOW_STOCK"));
    }
    
    @Test
    @DisplayName("Should sign up for notification successfully")
    void shouldSignUpForNotificationSuccessfully() throws Exception {
        // Given
        String productId = "product-1";
        NotificationSignupRequest request = new NotificationSignupRequest(productId, "test@example.com");
        
        StockNotification notification = new StockNotification();
        notification.setNotificationId("notification-1");
        notification.setProductId(productId);
        notification.setEmail("test@example.com");
        
        when(notificationService.signUpForNotification(productId, null, "test@example.com"))
            .thenReturn(notification);
        
        // When & Then
        mockMvc.perform(post("/api/v2/products/{id}/notify-me", productId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
        
        verify(notificationService).signUpForNotification(productId, null, "test@example.com");
    }
    
    @Test
    @DisplayName("Should get user notifications successfully")
    void shouldGetUserNotificationsSuccessfully() throws Exception {
        // Given
        String userId = "user-1";
        StockNotification notification = new StockNotification();
        notification.setNotificationId("notification-1");
        notification.setProductId("product-1");
        notification.setUserId(userId);
        notification.setStatus("PENDING");
        
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 0, "Category");
        
        when(notificationService.getUserNotifications(userId)).thenReturn(Arrays.asList(notification));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        
        // When & Then
        mockMvc.perform(get("/api/v2/stock/notifications")
            .param("userId", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].notificationId").value("notification-1"))
            .andExpect(jsonPath("$[0].productId").value("product-1"))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
    
    @Test
    @DisplayName("Should unsubscribe from notification successfully")
    void shouldUnsubscribeFromNotificationSuccessfully() throws Exception {
        // Given
        String notificationId = "notification-1";
        
        doNothing().when(notificationService).unsubscribe(notificationId);
        
        // When & Then
        mockMvc.perform(delete("/api/v2/stock/notifications/{notificationId}", notificationId))
            .andExpect(status().isNoContent());
        
        verify(notificationService).unsubscribe(notificationId);
    }
    
    @Test
    @DisplayName("Should get low stock products for admin")
    void shouldGetLowStockProductsForAdmin() throws Exception {
        // Given
        Product product1 = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        Product product2 = new Product("product-2", "Product 2", "Description", 20.0, 0, "Category");
        Product product3 = new Product("product-3", "Product 3", "Description", 30.0, 50, "Category");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));
        when(stockStatusService.calculateStockStatus(product1)).thenReturn(StockStatus.LOW_STOCK);
        when(stockStatusService.calculateStockStatus(product2)).thenReturn(StockStatus.OUT_OF_STOCK);
        when(stockStatusService.calculateStockStatus(product3)).thenReturn(StockStatus.IN_STOCK);
        when(stockStatusService.getStockStatusMessage(product1)).thenReturn("Low Stock - Only 5 left!");
        when(stockStatusService.getStockStatusMessage(product2)).thenReturn("Out of Stock");
        
        // When & Then
        mockMvc.perform(get("/api/v2/admin/stock/low-stock"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].productId").value("product-1"))
            .andExpect(jsonPath("$[0].status").value("LOW_STOCK"))
            .andExpect(jsonPath("$[1].productId").value("product-2"))
            .andExpect(jsonPath("$[1].status").value("OUT_OF_STOCK"));
    }
    
    @Test
    @DisplayName("Should get stock analytics for admin")
    void shouldGetStockAnalyticsForAdmin() throws Exception {
        // Given
        Product product1 = new Product("product-1", "Product 1", "Description", 10.0, 50, "Category");
        Product product2 = new Product("product-2", "Product 2", "Description", 20.0, 5, "Category");
        Product product3 = new Product("product-3", "Product 3", "Description", 30.0, 0, "Category");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));
        when(stockStatusService.calculateStockStatus(product1)).thenReturn(StockStatus.IN_STOCK);
        when(stockStatusService.calculateStockStatus(product2)).thenReturn(StockStatus.LOW_STOCK);
        when(stockStatusService.calculateStockStatus(product3)).thenReturn(StockStatus.OUT_OF_STOCK);
        when(notificationService.getUserNotifications("")).thenReturn(Arrays.asList());
        when(alertRepository.findByStatus("PENDING")).thenReturn(Arrays.asList());
        when(alertRepository.findByStatus("SENT")).thenReturn(Arrays.asList());
        
        // When & Then
        mockMvc.perform(get("/api/v2/admin/stock/analytics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalProducts").value(3))
            .andExpect(jsonPath("$.inStock").value(1))
            .andExpect(jsonPath("$.lowStock").value(1))
            .andExpect(jsonPath("$.outOfStock").value(1));
    }
}

