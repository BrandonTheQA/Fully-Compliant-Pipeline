package com.example.ecompoc.order.service;

import com.example.ecompoc.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for generating tracking numbers
 */
@Service
public class TrackingNumberService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingNumberService.class);
    
    /**
     * Generate a tracking number for an order
     * Format: ECOMPOC-{UUID}
     * 
     * @param order The order to generate tracking number for
     * @return Generated tracking number
     */
    public String generateTrackingNumber(Order order) {
        String trackingNumber = "ECOMPOC-" + UUID.randomUUID().toString().toUpperCase().replace("-", "");
        logger.info("Generated tracking number {} for order {}", trackingNumber, order.getId());
        return trackingNumber;
    }
}
