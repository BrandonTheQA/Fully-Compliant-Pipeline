package com.example.ecompoc.order.service;

import com.example.ecompoc.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for calculating estimated delivery dates
 */
@Service
public class DeliveryDateCalculatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeliveryDateCalculatorService.class);
    
    @Value("${order-tracking.processing-days:2}")
    private int defaultProcessingDays;
    
    /**
     * Calculate estimated delivery date for an order
     * 
     * @param order The order to calculate delivery date for
     * @return Estimated delivery date
     */
    public LocalDateTime calculateEstimatedDelivery(Order order) {
        LocalDateTime startDate = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();
        
        // Processing time: 1-2 business days
        LocalDateTime afterProcessing = addBusinessDays(startDate, defaultProcessingDays);
        
        // Shipping time depends on shipping method and region
        String shippingMethod = order.getShippingMethod();
        String region = extractRegionFromAddress(order.getShippingAddress());
        
        int shippingDays = calculateShippingDays(shippingMethod, region);
        LocalDateTime estimatedDelivery = addBusinessDays(afterProcessing, shippingDays);
        
        logger.debug("Calculated estimated delivery date {} for order {} (processing: {} days, shipping: {} days)", 
            estimatedDelivery, order.getId(), defaultProcessingDays, shippingDays);
        
        return estimatedDelivery;
    }
    
    /**
     * Calculate shipping days based on method and region
     */
    private int calculateShippingDays(String shippingMethod, String region) {
        if (shippingMethod == null || shippingMethod.isEmpty()) {
            shippingMethod = "STANDARD";
        }
        
        String normalizedMethod = shippingMethod.toUpperCase();
        String normalizedRegion = region != null ? region.toUpperCase() : "US";
        
        // Standard shipping
        if ("STANDARD".equals(normalizedMethod)) {
            if ("CA".equals(normalizedRegion)) {
                return 5; // 5-7 business days for CA
            } else {
                return 3; // 3-5 business days for US
            }
        }
        
        // Express shipping
        if ("EXPRESS".equals(normalizedMethod)) {
            if ("CA".equals(normalizedRegion)) {
                return 2; // 2-3 business days for CA
            } else {
                return 1; // 1-2 business days for US
            }
        }
        
        // Default to standard shipping
        return normalizedRegion.equals("CA") ? 5 : 3;
    }
    
    /**
     * Extract region from shipping address (simple implementation)
     */
    private String extractRegionFromAddress(String shippingAddress) {
        if (shippingAddress == null || shippingAddress.isEmpty()) {
            return "US"; // Default to US
        }
        
        String upperAddress = shippingAddress.toUpperCase();
        if (upperAddress.contains("CANADA") || upperAddress.contains("CA ")) {
            return "CA";
        }
        return "US";
    }
    
    /**
     * Add business days to a date (excluding weekends)
     */
    private LocalDateTime addBusinessDays(LocalDateTime date, int businessDays) {
        LocalDateTime result = date;
        int addedDays = 0;
        
        while (addedDays < businessDays) {
            result = result.plusDays(1);
            DayOfWeek dayOfWeek = result.getDayOfWeek();
            
            // Skip weekends
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                addedDays++;
            }
        }
        
        return result;
    }
}
