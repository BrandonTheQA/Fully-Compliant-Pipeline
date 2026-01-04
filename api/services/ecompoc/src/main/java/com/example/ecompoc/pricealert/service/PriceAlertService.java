package com.example.ecompoc.pricealert.service;

import com.example.ecompoc.pricealert.dto.CreatePriceAlertRequest;
import com.example.ecompoc.pricealert.dto.PriceAlertListResponse;
import com.example.ecompoc.pricealert.dto.PriceAlertResponse;
import com.example.ecompoc.pricealert.dto.PriceHistoryResponse;
import com.example.ecompoc.pricealert.dto.UpdatePriceAlertRequest;
import com.example.ecompoc.pricealert.model.AlertStatus;
import com.example.ecompoc.pricealert.model.NotificationFrequency;
import com.example.ecompoc.pricealert.model.PriceAlert;
import com.example.ecompoc.pricealert.model.PriceHistory;
import com.example.ecompoc.pricealert.repository.PriceAlertRepository;
import com.example.ecompoc.pricealert.repository.PriceHistoryRepository;
import com.example.ecompoc.pricealert.service.PriceAlertEmailService;
import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing price alerts
 */
@Service
public class PriceAlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceAlertService.class);
    
    private final PriceAlertRepository priceAlertRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;
    private PriceAlertEmailService emailService;
    
    @Value("${price-alert.enabled:true}")
    private boolean priceAlertEnabled;
    
    public PriceAlertService(PriceAlertRepository priceAlertRepository,
                            PriceHistoryRepository priceHistoryRepository,
                            ProductRepository productRepository) {
        this.priceAlertRepository = priceAlertRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.productRepository = productRepository;
    }
    
    @Autowired(required = false)
    public void setEmailService(PriceAlertEmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Create a new price alert
     */
    @Transactional
    public PriceAlertResponse createPriceAlert(CreatePriceAlertRequest request) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled");
            return null;
        }
        
        // Validate request
        if (request == null || request.getProductId() == null || request.getEmail() == null) {
            logger.warn("Invalid price alert request: missing required fields");
            return null;
        }
        
        // Validate product exists
        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) {
            logger.warn("Product not found: productId={}", request.getProductId());
            return null;
        }
        
        Product product = productOpt.get();
        BigDecimal currentPrice = product.getPriceDecimal();
        
        // Check for duplicate alert (same product + email)
        Optional<PriceAlert> existingAlert = priceAlertRepository.findByProductIdAndUserEmail(
            request.getProductId(), request.getEmail());
        if (existingAlert.isPresent() && "ACTIVE".equals(existingAlert.get().getStatus())) {
            logger.warn("Active price alert already exists for product={}, email={}", 
                request.getProductId(), request.getEmail());
            return mapToResponse(existingAlert.get());
        }
        
        // Create new alert
        PriceAlert alert = new PriceAlert();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setProductId(request.getProductId());
        alert.setUserEmail(request.getEmail());
        alert.setUserId(request.getUserId());
        alert.setCurrentPrice(currentPrice);
        
        if (request.getTargetPrice() != null) {
            alert.setTargetPrice(BigDecimal.valueOf(request.getTargetPrice()));
        }
        
        // Set notification frequency (default to IMMEDIATE)
        String frequency = request.getNotificationFrequency();
        if (frequency == null || frequency.isEmpty()) {
            frequency = NotificationFrequency.IMMEDIATE.name();
        }
        alert.setNotificationFrequency(frequency);
        
        alert.setStatus(AlertStatus.ACTIVE.name());
        LocalDateTime now = LocalDateTime.now();
        alert.setCreatedAt(now);
        alert.setUpdatedAt(now);
        
        // Save to database
        PriceAlert savedAlert = priceAlertRepository.save(alert);
        
        logger.info("Created price alert: alertId={}, productId={}, email={}", 
            savedAlert.getAlertId(), savedAlert.getProductId(), savedAlert.getUserEmail());
        
        // Send confirmation email
        if (emailService != null) {
            try {
                emailService.sendConfirmationEmail(savedAlert);
            } catch (Exception e) {
                logger.error("Failed to send confirmation email for alert: alertId={}", 
                    savedAlert.getAlertId(), e);
            }
        }
        
        return mapToResponse(savedAlert);
    }
    
    /**
     * Get price alerts for a user (by email and optionally userId)
     */
    public PriceAlertListResponse getPriceAlerts(String email, String userId) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled");
            return new PriceAlertListResponse(List.of());
        }
        
        List<PriceAlert> alerts;
        if (userId != null && !userId.isEmpty()) {
            alerts = priceAlertRepository.findByUserId(userId);
        } else if (email != null && !email.isEmpty()) {
            alerts = priceAlertRepository.findByUserEmail(email);
        } else {
            logger.warn("Invalid request: email or userId required");
            return new PriceAlertListResponse(List.of());
        }
        
        List<PriceAlertResponse> responseList = alerts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PriceAlertListResponse(responseList);
    }
    
    /**
     * Get a specific price alert by ID
     */
    public PriceAlertResponse getPriceAlert(String alertId) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled");
            return null;
        }
        
        return priceAlertRepository.findById(alertId)
            .map(this::mapToResponse)
            .orElse(null);
    }
    
    /**
     * Update a price alert
     */
    @Transactional
    public PriceAlertResponse updatePriceAlert(String alertId, UpdatePriceAlertRequest request) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled");
            return null;
        }
        
        Optional<PriceAlert> alertOpt = priceAlertRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            logger.warn("Price alert not found: alertId={}", alertId);
            return null;
        }
        
        PriceAlert alert = alertOpt.get();
        
        if (request.getTargetPrice() != null) {
            alert.setTargetPrice(BigDecimal.valueOf(request.getTargetPrice()));
        }
        
        if (request.getNotificationFrequency() != null) {
            alert.setNotificationFrequency(request.getNotificationFrequency());
        }
        
        if (request.getStatus() != null) {
            alert.setStatus(request.getStatus());
        }
        
        alert.setUpdatedAt(LocalDateTime.now());
        
        PriceAlert updatedAlert = priceAlertRepository.save(alert);
        
        logger.info("Updated price alert: alertId={}", alertId);
        
        return mapToResponse(updatedAlert);
    }
    
    /**
     * Delete (cancel) a price alert
     */
    @Transactional
    public void deletePriceAlert(String alertId) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled");
            return;
        }
        
        Optional<PriceAlert> alertOpt = priceAlertRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            logger.warn("Price alert not found: alertId={}", alertId);
            return;
        }
        
        PriceAlert alert = alertOpt.get();
        alert.setStatus(AlertStatus.CANCELLED.name());
        alert.setUpdatedAt(LocalDateTime.now());
        priceAlertRepository.save(alert);
        
        logger.info("Cancelled price alert: alertId={}", alertId);
    }
    
    /**
     * Get price history for a product
     */
    public List<PriceHistoryResponse> getPriceHistory(String productId) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled");
            return List.of();
        }
        
        List<PriceHistory> historyList = priceHistoryRepository.findByProductIdOrderByChangedAtDesc(productId);
        
        return historyList.stream()
            .map(this::mapHistoryToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Map entity to response DTO
     */
    private PriceAlertResponse mapToResponse(PriceAlert alert) {
        PriceAlertResponse response = new PriceAlertResponse();
        response.setAlertId(alert.getAlertId());
        response.setProductId(alert.getProductId());
        response.setUserEmail(alert.getUserEmail());
        response.setUserId(alert.getUserId());
        response.setCurrentPrice(alert.getCurrentPrice() != null ? 
            alert.getCurrentPrice().doubleValue() : null);
        response.setTargetPrice(alert.getTargetPrice() != null ? 
            alert.getTargetPrice().doubleValue() : null);
        response.setNotificationFrequency(alert.getNotificationFrequency());
        response.setStatus(alert.getStatus());
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (alert.getCreatedAt() != null) {
            response.setCreatedAt(alert.getCreatedAt().format(formatter));
        }
        if (alert.getLastTriggeredAt() != null) {
            response.setLastTriggeredAt(alert.getLastTriggeredAt().format(formatter));
        }
        if (alert.getUpdatedAt() != null) {
            response.setUpdatedAt(alert.getUpdatedAt().format(formatter));
        }
        
        return response;
    }
    
    /**
     * Map price history entity to response DTO
     */
    private PriceHistoryResponse mapHistoryToResponse(PriceHistory history) {
        PriceHistoryResponse response = new PriceHistoryResponse();
        response.setPriceHistoryId(history.getPriceHistoryId());
        response.setProductId(history.getProductId());
        response.setPrice(history.getPrice() != null ? history.getPrice().doubleValue() : null);
        response.setPreviousPrice(history.getPreviousPrice() != null ? 
            history.getPreviousPrice().doubleValue() : null);
        response.setChangeType(history.getChangeType());
        response.setChangePercentage(history.getChangePercentage() != null ? 
            history.getChangePercentage().doubleValue() : null);
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (history.getChangedAt() != null) {
            response.setChangedAt(history.getChangedAt().format(formatter));
        }
        
        return response;
    }
}

