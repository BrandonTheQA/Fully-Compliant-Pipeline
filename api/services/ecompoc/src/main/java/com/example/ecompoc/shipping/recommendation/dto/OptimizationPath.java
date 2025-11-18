package com.example.ecompoc.shipping.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing a path to reach free shipping threshold
 */
public class OptimizationPath {
    
    @JsonProperty("products")
    private List<RecommendedProduct> products;
    
    @JsonProperty("totalCost")
    private BigDecimal totalCost;
    
    @JsonProperty("savingsAmount")
    private BigDecimal savingsAmount;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("pathType")
    private String pathType; // "single", "bundle", "category"
    
    // Default constructor
    public OptimizationPath() {}
    
    // Constructor with all fields
    public OptimizationPath(List<RecommendedProduct> products, BigDecimal totalCost, 
                           BigDecimal savingsAmount, String message, String pathType) {
        this.products = products;
        this.totalCost = totalCost;
        this.savingsAmount = savingsAmount;
        this.message = message;
        this.pathType = pathType;
    }
    
    // Getters and Setters
    public List<RecommendedProduct> getProducts() {
        return products;
    }
    
    public void setProducts(List<RecommendedProduct> products) {
        this.products = products;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public BigDecimal getSavingsAmount() {
        return savingsAmount;
    }
    
    public void setSavingsAmount(BigDecimal savingsAmount) {
        this.savingsAmount = savingsAmount;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPathType() {
        return pathType;
    }
    
    public void setPathType(String pathType) {
        this.pathType = pathType;
    }
}

