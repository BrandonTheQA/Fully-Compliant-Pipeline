package com.example.ecompoc.shipping.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * DTO for a recommended product that can help reach free shipping threshold
 */
public class RecommendedProduct {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("savingsMessage")
    private String savingsMessage;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    // Default constructor
    public RecommendedProduct() {}
    
    // Constructor with all fields
    public RecommendedProduct(String id, String name, String description, BigDecimal price, 
                             String category, String savingsMessage, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.savingsMessage = savingsMessage;
        this.imageUrl = imageUrl;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSavingsMessage() {
        return savingsMessage;
    }
    
    public void setSavingsMessage(String savingsMessage) {
        this.savingsMessage = savingsMessage;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

