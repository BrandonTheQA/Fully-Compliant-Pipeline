package com.example.ecompoc.order.controller;

import com.example.ecompoc.order.service.OrderTrackingStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST Controller for order tracking Server-Sent Events (SSE)
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Tracking Stream", description = "Real-time order tracking updates via Server-Sent Events")
public class OrderTrackingStreamController {
    
    private final OrderTrackingStreamService streamService;
    
    public OrderTrackingStreamController(OrderTrackingStreamService streamService) {
        this.streamService = streamService;
    }
    
    /**
     * GET /api/orders/{id}/tracking/stream - Subscribe to real-time order tracking updates
     */
    @Operation(
            summary = "Subscribe to order tracking updates",
            description = "Establishes a Server-Sent Events (SSE) connection to receive real-time order status updates"
    )
    @GetMapping(value = "/{id}/tracking/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToOrderTracking(
            @Parameter(description = "Order ID", required = true, example = "123")
            @PathVariable String id) {
        SseEmitter emitter = streamService.subscribe(id);
        return ResponseEntity.ok(emitter);
    }
}
