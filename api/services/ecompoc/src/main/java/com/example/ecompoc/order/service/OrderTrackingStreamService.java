package com.example.ecompoc.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing Server-Sent Events (SSE) connections for order tracking
 */
@Service
public class OrderTrackingStreamService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderTrackingStreamService.class);
    
    // Map of orderId -> Set of SseEmitters
    private final Map<String, Map<String, SseEmitter>> orderSubscribers = new ConcurrentHashMap<>();
    
    /**
     * Subscribe to order updates
     */
    public SseEmitter subscribe(String orderId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minute timeout
        
        String connectionId = java.util.UUID.randomUUID().toString();
        
        orderSubscribers.computeIfAbsent(orderId, k -> new ConcurrentHashMap<>())
                .put(connectionId, emitter);
        
        // Handle completion and timeout
        emitter.onCompletion(() -> {
            logger.debug("SSE connection completed for order {} connection {}", orderId, connectionId);
            removeSubscriber(orderId, connectionId);
        });
        
        emitter.onTimeout(() -> {
            logger.debug("SSE connection timeout for order {} connection {}", orderId, connectionId);
            removeSubscriber(orderId, connectionId);
        });
        
        emitter.onError((ex) -> {
            logger.error("SSE connection error for order {} connection {}", orderId, connectionId, ex);
            removeSubscriber(orderId, connectionId);
        });
        
        // Send initial connection confirmation
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to order tracking stream for order " + orderId));
        } catch (IOException e) {
            logger.error("Error sending initial SSE message for order {}", orderId, e);
            removeSubscriber(orderId, connectionId);
        }
        
        logger.info("New SSE subscriber for order {}: connection {}", orderId, connectionId);
        return emitter;
    }
    
    /**
     * Broadcast status update to all subscribers for an order
     */
    public void broadcastStatusUpdate(String orderId, String status, String message) {
        Map<String, SseEmitter> subscribers = orderSubscribers.get(orderId);
        if (subscribers == null || subscribers.isEmpty()) {
            logger.debug("No subscribers for order {}", orderId);
            return;
        }
        
        logger.debug("Broadcasting status update to {} subscribers for order {}", subscribers.size(), orderId);
        
        subscribers.entrySet().removeIf(entry -> {
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(SseEmitter.event()
                        .name("status-update")
                        .data("{\"orderId\":\"" + orderId + "\",\"status\":\"" + status + "\",\"message\":\"" + message + "\"}"));
                return false; // Keep the subscriber
            } catch (IOException e) {
                logger.warn("Error sending SSE update to subscriber {} for order {}, removing", entry.getKey(), orderId, e);
                return true; // Remove the subscriber
            }
        });
    }
    
    /**
     * Remove a subscriber
     */
    private void removeSubscriber(String orderId, String connectionId) {
        Map<String, SseEmitter> subscribers = orderSubscribers.get(orderId);
        if (subscribers != null) {
            subscribers.remove(connectionId);
            if (subscribers.isEmpty()) {
                orderSubscribers.remove(orderId);
            }
        }
    }
    
    /**
     * Get number of active subscribers for an order
     */
    public int getSubscriberCount(String orderId) {
        Map<String, SseEmitter> subscribers = orderSubscribers.get(orderId);
        return subscribers != null ? subscribers.size() : 0;
    }
}
