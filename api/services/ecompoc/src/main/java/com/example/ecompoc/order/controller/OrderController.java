package com.example.ecompoc.order.controller;

import com.example.ecompoc.order.dto.CreateOrderRequest;
import com.example.ecompoc.order.dto.OrderResponse;
import com.example.ecompoc.order.dto.OrderStatusHistoryResponse;
import com.example.ecompoc.order.dto.OrderTrackingResponse;
import com.example.ecompoc.order.dto.UpdateOrderStatusRequest;
import com.example.ecompoc.order.enums.OrderStatus;
import com.example.ecompoc.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for order management endpoints
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Orders", description = "Order management API endpoints")
public class OrderController {
    
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * POST /api/orders - Create a new order
     */
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with the provided order details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Order creation request", required = true)
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    /**
     * GET /api/orders/{id} - Get order details by ID
     */
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves order details for the specified order ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID", required = true, example = "123")
            @PathVariable String id) {
        OrderResponse order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * GET /api/orders/user/{userId} - List user's orders
     */
    @Operation(
            summary = "Get user orders",
            description = "Retrieves all orders for the specified user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @Parameter(description = "User ID", required = true, example = "user123")
            @PathVariable String userId) {
        List<OrderResponse> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * GET /api/orders/{id}/tracking - Get order tracking information
     */
    @Operation(
            summary = "Get order tracking information",
            description = "Retrieves comprehensive tracking information for the specified order ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking information found",
                    content = @Content(schema = @Schema(implementation = OrderTrackingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/orders/{id}/tracking")
    public ResponseEntity<OrderTrackingResponse> getOrderTracking(
            @Parameter(description = "Order ID", required = true, example = "123")
            @PathVariable String id) {
        OrderTrackingResponse tracking = orderService.getOrderTracking(id);
        return ResponseEntity.ok(tracking);
    }
    
    /**
     * PUT /api/orders/{id}/status - Update order status
     */
    @Operation(
            summary = "Update order status",
            description = "Updates the status of an order (admin/internal use). Triggers notifications and status history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID", required = true, example = "123")
            @PathVariable String id,
            @Parameter(description = "Status update request", required = true)
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        OrderResponse order = orderService.updateOrderStatus(id, newStatus, request.getLocation(), request.getNotes());
        return ResponseEntity.ok(order);
    }
    
    /**
     * GET /api/orders/{id}/status-history - Get order status history
     */
    @Operation(
            summary = "Get order status history",
            description = "Retrieves the complete status history timeline for the specified order ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderStatusHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/orders/{id}/status-history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getOrderStatusHistory(
            @Parameter(description = "Order ID", required = true, example = "123")
            @PathVariable String id) {
        List<OrderStatusHistoryResponse> history = orderService.getOrderStatusHistoryResponse(id);
        return ResponseEntity.ok(history);
    }
}

