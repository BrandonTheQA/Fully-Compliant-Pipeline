package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.dto.ReturnAnalyticsResponse;
import com.example.ecompoc.returns.enums.ReturnReason;
import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.model.ReturnItem;
import com.example.ecompoc.returns.repository.ReturnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for aggregating return metrics, analytics, and reporting data
 */
@Service
public class ReturnAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReturnAnalyticsService.class);
    
    private final ReturnRepository returnRepository;
    
    @Autowired
    public ReturnAnalyticsService(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }
    
    /**
     * Get comprehensive return analytics
     */
    public ReturnAnalyticsResponse getAnalytics() {
        List<Return> allReturns = returnRepository.findAll();
        
        ReturnAnalyticsResponse response = new ReturnAnalyticsResponse();
        
        // Total returns and value
        response.setTotalReturns((long) allReturns.size());
        BigDecimal totalValue = allReturns.stream()
            .filter(r -> r.getRefundAmountDecimal() != null)
            .map(Return::getRefundAmountDecimal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalReturnValue(totalValue.doubleValue());
        
        // Average return processing time
        double avgProcessingTime = calculateAverageProcessingTime(allReturns);
        response.setAverageReturnProcessingTime(avgProcessingTime);
        
        // Return rate (would need total orders to calculate accurately)
        // For now, we'll set a placeholder
        response.setReturnRate(0.0); // TODO: Calculate from total orders
        
        // Return reasons distribution
        Map<String, Long> reasonsDistribution = calculateReturnReasonsDistribution(allReturns);
        response.setReturnReasonsDistribution(reasonsDistribution);
        
        // Return rate by product
        List<ReturnAnalyticsResponse.ProductReturnRate> productReturnRates = 
            calculateProductReturnRates(allReturns);
        response.setReturnRateByProduct(productReturnRates);
        
        // Returns by status
        Map<String, Long> returnsByStatus = calculateReturnsByStatus(allReturns);
        response.setReturnsByStatus(returnsByStatus);
        
        // Returns by month
        List<ReturnAnalyticsResponse.MonthlyReturnStats> monthlyStats = 
            calculateMonthlyReturnStats(allReturns);
        response.setReturnsByMonth(monthlyStats);
        
        return response;
    }
    
    /**
     * Calculate average return processing time in days
     */
    private double calculateAverageProcessingTime(List<Return> returns) {
        List<Long> processingTimes = new ArrayList<>();
        
        for (Return returnEntity : returns) {
            if (returnEntity.getStatus() == ReturnStatus.REFUNDED || 
                returnEntity.getStatus() == ReturnStatus.COMPLETED) {
                
                LocalDateTime createdAt = returnEntity.getCreatedAt();
                LocalDateTime completedAt = returnEntity.getRefundDate() != null 
                    ? returnEntity.getRefundDate() 
                    : returnEntity.getUpdatedAt();
                
                if (createdAt != null && completedAt != null) {
                    long days = java.time.Duration.between(createdAt, completedAt).toDays();
                    processingTimes.add(days);
                }
            }
        }
        
        if (processingTimes.isEmpty()) {
            return 0.0;
        }
        
        return processingTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calculate return reasons distribution
     */
    private Map<String, Long> calculateReturnReasonsDistribution(List<Return> returns) {
        Map<String, Long> distribution = new HashMap<>();
        
        for (Return returnEntity : returns) {
            for (ReturnItem item : returnEntity.getItems()) {
                String reason = item.getReturnReason().name();
                distribution.put(reason, distribution.getOrDefault(reason, 0L) + 1);
            }
        }
        
        return distribution;
    }
    
    /**
     * Calculate return rate by product
     */
    private List<ReturnAnalyticsResponse.ProductReturnRate> calculateProductReturnRates(List<Return> returns) {
        Map<String, Long> productReturnCounts = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        
        for (Return returnEntity : returns) {
            for (ReturnItem item : returnEntity.getItems()) {
                String productId = item.getProductId();
                productReturnCounts.put(productId, 
                    productReturnCounts.getOrDefault(productId, 0L) + item.getQuantity());
                productNames.put(productId, item.getProductName());
            }
        }
        
        List<ReturnAnalyticsResponse.ProductReturnRate> productRates = new ArrayList<>();
        for (Map.Entry<String, Long> entry : productReturnCounts.entrySet()) {
            ReturnAnalyticsResponse.ProductReturnRate rate = 
                new ReturnAnalyticsResponse.ProductReturnRate();
            rate.setProductId(entry.getKey());
            rate.setProductName(productNames.get(entry.getKey()));
            rate.setTotalReturns(entry.getValue());
            rate.setReturnRate(0.0); // TODO: Calculate actual return rate from total orders
            productRates.add(rate);
        }
        
        // Sort by total returns descending
        productRates.sort((a, b) -> Long.compare(b.getTotalReturns(), a.getTotalReturns()));
        
        return productRates;
    }
    
    /**
     * Calculate returns by status
     */
    private Map<String, Long> calculateReturnsByStatus(List<Return> returns) {
        return returns.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStatus().name(),
                Collectors.counting()
            ));
    }
    
    /**
     * Calculate monthly return statistics
     */
    private List<ReturnAnalyticsResponse.MonthlyReturnStats> calculateMonthlyReturnStats(List<Return> returns) {
        Map<String, ReturnAnalyticsResponse.MonthlyReturnStats> monthlyMap = new HashMap<>();
        
        for (Return returnEntity : returns) {
            LocalDateTime createdAt = returnEntity.getCreatedAt();
            if (createdAt == null) continue;
            
            String monthKey = createdAt.getYear() + "-" + 
                String.format("%02d", createdAt.getMonthValue());
            
            ReturnAnalyticsResponse.MonthlyReturnStats stats = monthlyMap.getOrDefault(monthKey,
                new ReturnAnalyticsResponse.MonthlyReturnStats());
            
            stats.setYear(createdAt.getYear());
            stats.setMonth(createdAt.getMonth().name());
            stats.setTotalReturns(stats.getTotalReturns() != null ? 
                stats.getTotalReturns() + 1 : 1L);
            
            BigDecimal returnValue = returnEntity.getRefundAmountDecimal() != null 
                ? returnEntity.getRefundAmountDecimal() 
                : BigDecimal.ZERO;
            stats.setTotalValue(stats.getTotalValue() != null ? 
                stats.getTotalValue() + returnValue.doubleValue() : returnValue.doubleValue());
            
            monthlyMap.put(monthKey, stats);
        }
        
        List<ReturnAnalyticsResponse.MonthlyReturnStats> monthlyStats = new ArrayList<>(monthlyMap.values());
        monthlyStats.sort((a, b) -> {
            int yearCompare = Integer.compare(b.getYear(), a.getYear());
            if (yearCompare != 0) return yearCompare;
            return Month.valueOf(b.getMonth()).compareTo(Month.valueOf(a.getMonth()));
        });
        
        return monthlyStats;
    }
}

