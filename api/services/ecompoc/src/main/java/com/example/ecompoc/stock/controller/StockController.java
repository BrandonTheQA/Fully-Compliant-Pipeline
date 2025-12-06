package com.example.ecompoc.stock.controller;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.dto.*;
import com.example.ecompoc.stock.model.StockNotification;
import com.example.ecompoc.stock.model.StockStatus;
import com.example.ecompoc.stock.repository.LowStockAlertRepository;
import com.example.ecompoc.stock.service.StockNotificationService;
import com.example.ecompoc.stock.service.StockStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for stock management endpoints (v2 API)
 */
@RestController
@RequestMapping("/api/v2")
@Tag(name = "Stock Management", description = "Stock status and notification API endpoints")
public class StockController {
    
    private final StockStatusService stockStatusService;
    private final StockNotificationService notificationService;
    private final ProductRepository productRepository;
    private final LowStockAlertRepository alertRepository;
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    public StockController(StockStatusService stockStatusService,
                          StockNotificationService notificationService,
                          ProductRepository productRepository,
                          LowStockAlertRepository alertRepository) {
        this.stockStatusService = stockStatusService;
        this.notificationService = notificationService;
        this.productRepository = productRepository;
        this.alertRepository = alertRepository;
    }
    
    /**
     * GET /api/v2/products/{id}/stock - Get stock status for a product
     */
    @Operation(
            summary = "Get stock status for a product",
            description = "Retrieves current stock status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK) for the specified product"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StockStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/products/{id}/stock")
    public ResponseEntity<StockStatusResponse> getStockStatus(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String id) {
        if (!stockManagementEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        StockStatus status = stockStatusService.calculateStockStatus(product);
        String message = stockStatusService.getStockStatusMessage(product);
        
        StockStatusResponse response = new StockStatusResponse(
            product.getId(),
            status != null ? status.name() : "UNKNOWN",
            product.getQuantity(),
            product.getLowStockThreshold(),
            message
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/v2/products/stock/bulk - Bulk stock status query
     */
    @Operation(
            summary = "Get bulk stock status for multiple products",
            description = "Retrieves stock status for multiple products in a single request"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock statuses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BulkStockStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/products/stock/bulk")
    public ResponseEntity<BulkStockStatusResponse> getBulkStockStatus(
            @Parameter(description = "Bulk stock status request", required = true)
            @Valid @RequestBody BulkStockStatusRequest request) {
        if (!stockManagementEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, StockStatus> statusMap = stockStatusService.getBulkStockStatus(request.getProductIds());
        List<Product> products = productRepository.findAllById(request.getProductIds());
        
        List<StockStatusResponse> statuses = products.stream()
            .map(product -> {
                StockStatus status = statusMap.get(product.getId());
                String message = stockStatusService.getStockStatusMessage(product);
                return new StockStatusResponse(
                    product.getId(),
                    status != null ? status.name() : "UNKNOWN",
                    product.getQuantity(),
                    product.getLowStockThreshold(),
                    message
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new BulkStockStatusResponse(statuses));
    }
    
    /**
     * POST /api/v2/products/{id}/notify-me - Sign up for back-in-stock notifications
     */
    @Operation(
            summary = "Sign up for back-in-stock notifications",
            description = "Subscribes to receive email notifications when a product becomes available"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully subscribed to notifications"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Product not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/products/{id}/notify-me")
    public ResponseEntity<Void> signUpForNotification(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Notification signup request", required = true)
            @Valid @RequestBody NotificationSignupRequest request) {
        if (!stockManagementEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        if (!id.equals(request.getProductId())) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            notificationService.signUpForNotification(request.getProductId(), null, request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * GET /api/v2/stock/notifications - Get user's notification subscriptions
     */
    @Operation(
            summary = "Get user's notification subscriptions",
            description = "Retrieves all stock notification subscriptions for the specified user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/stock/notifications")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId) {
        if (!stockManagementEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        List<StockNotification> notifications = notificationService.getUserNotifications(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        List<NotificationResponse> responses = notifications.stream()
            .map(notification -> {
                Product product = productRepository.findById(notification.getProductId()).orElse(null);
                String productName = product != null ? product.getName() : "Unknown Product";
                
                return new NotificationResponse(
                    notification.getNotificationId(),
                    notification.getProductId(),
                    productName,
                    notification.getStatus(),
                    notification.getSignupDate() != null 
                        ? notification.getSignupDate().format(formatter) 
                        : null,
                    notification.getNotifiedDate() != null 
                        ? notification.getNotifiedDate().format(formatter) 
                        : null
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * DELETE /api/v2/stock/notifications/{notificationId} - Unsubscribe from notification
     */
    @Operation(
            summary = "Unsubscribe from stock notification",
            description = "Removes a stock notification subscription"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully unsubscribed"),
            @ApiResponse(responseCode = "404", description = "Notification not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/stock/notifications/{notificationId}")
    public ResponseEntity<Void> unsubscribe(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable String notificationId) {
        if (!stockManagementEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        try {
            notificationService.unsubscribe(notificationId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * GET /api/v2/admin/stock/low-stock - Admin endpoint for low stock products
     */
    @Operation(
            summary = "Get low stock products (Admin)",
            description = "Retrieves all products currently below their low stock threshold"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/admin/stock/low-stock")
    public ResponseEntity<List<StockStatusResponse>> getLowStockProducts() {
        if (!stockManagementEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        // Get all products and filter for low stock
        List<Product> allProducts = productRepository.findAll();
        List<StockStatusResponse> lowStockProducts = allProducts.stream()
            .filter(product -> {
                StockStatus status = stockStatusService.calculateStockStatus(product);
                return status == StockStatus.LOW_STOCK || status == StockStatus.OUT_OF_STOCK;
            })
            .map(product -> {
                StockStatus status = stockStatusService.calculateStockStatus(product);
                String message = stockStatusService.getStockStatusMessage(product);
                return new StockStatusResponse(
                    product.getId(),
                    status.name(),
                    product.getQuantity(),
                    product.getLowStockThreshold(),
                    message
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(lowStockProducts);
    }
    
    /**
     * GET /api/v2/admin/stock/analytics - Stock analytics dashboard data
     */
    @Operation(
            summary = "Get stock analytics (Admin)",
            description = "Retrieves analytics data for stock management dashboard"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/admin/stock/analytics")
    public ResponseEntity<Map<String, Object>> getStockAnalytics() {
        if (!stockManagementEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        // Basic analytics - can be expanded later
        List<Product> allProducts = productRepository.findAll();
        long totalProducts = allProducts.size();
        long inStock = allProducts.stream()
            .filter(p -> stockStatusService.calculateStockStatus(p) == StockStatus.IN_STOCK)
            .count();
        long lowStock = allProducts.stream()
            .filter(p -> stockStatusService.calculateStockStatus(p) == StockStatus.LOW_STOCK)
            .count();
        long outOfStock = allProducts.stream()
            .filter(p -> stockStatusService.calculateStockStatus(p) == StockStatus.OUT_OF_STOCK)
            .count();
        
        long pendingNotifications = notificationService.getUserNotifications("").size(); // This is a placeholder
        long activeAlerts = alertRepository.findByStatus("PENDING").size() + 
                           alertRepository.findByStatus("SENT").size();
        
        Map<String, Object> analytics = Map.of(
            "totalProducts", totalProducts,
            "inStock", inStock,
            "lowStock", lowStock,
            "outOfStock", outOfStock,
            "pendingNotifications", pendingNotifications,
            "activeAlerts", activeAlerts
        );
        
        return ResponseEntity.ok(analytics);
    }
}

