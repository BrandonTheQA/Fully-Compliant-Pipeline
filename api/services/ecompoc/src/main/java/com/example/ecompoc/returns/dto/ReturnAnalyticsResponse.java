package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for return analytics
 */
public class ReturnAnalyticsResponse {
    
    @JsonProperty("totalReturns")
    private Long totalReturns;
    
    @JsonProperty("totalReturnValue")
    private Double totalReturnValue;
    
    @JsonProperty("averageReturnProcessingTime")
    private Double averageReturnProcessingTime;
    
    @JsonProperty("returnRate")
    private Double returnRate;
    
    @JsonProperty("returnReasonsDistribution")
    private Map<String, Long> returnReasonsDistribution;
    
    @JsonProperty("returnRateByProduct")
    private List<ProductReturnRate> returnRateByProduct;
    
    @JsonProperty("returnsByStatus")
    private Map<String, Long> returnsByStatus;
    
    @JsonProperty("returnsByMonth")
    private List<MonthlyReturnStats> returnsByMonth;
    
    public ReturnAnalyticsResponse() {}
    
    // Getters and Setters
    public Long getTotalReturns() {
        return totalReturns;
    }
    
    public void setTotalReturns(Long totalReturns) {
        this.totalReturns = totalReturns;
    }
    
    public Double getTotalReturnValue() {
        return totalReturnValue;
    }
    
    public void setTotalReturnValue(Double totalReturnValue) {
        this.totalReturnValue = totalReturnValue;
    }
    
    public Double getAverageReturnProcessingTime() {
        return averageReturnProcessingTime;
    }
    
    public void setAverageReturnProcessingTime(Double averageReturnProcessingTime) {
        this.averageReturnProcessingTime = averageReturnProcessingTime;
    }
    
    public Double getReturnRate() {
        return returnRate;
    }
    
    public void setReturnRate(Double returnRate) {
        this.returnRate = returnRate;
    }
    
    public Map<String, Long> getReturnReasonsDistribution() {
        return returnReasonsDistribution;
    }
    
    public void setReturnReasonsDistribution(Map<String, Long> returnReasonsDistribution) {
        this.returnReasonsDistribution = returnReasonsDistribution;
    }
    
    public List<ProductReturnRate> getReturnRateByProduct() {
        return returnRateByProduct;
    }
    
    public void setReturnRateByProduct(List<ProductReturnRate> returnRateByProduct) {
        this.returnRateByProduct = returnRateByProduct;
    }
    
    public Map<String, Long> getReturnsByStatus() {
        return returnsByStatus;
    }
    
    public void setReturnsByStatus(Map<String, Long> returnsByStatus) {
        this.returnsByStatus = returnsByStatus;
    }
    
    public List<MonthlyReturnStats> getReturnsByMonth() {
        return returnsByMonth;
    }
    
    public void setReturnsByMonth(List<MonthlyReturnStats> returnsByMonth) {
        this.returnsByMonth = returnsByMonth;
    }
    
    /**
     * Inner class for product return rate
     */
    public static class ProductReturnRate {
        @JsonProperty("productId")
        private String productId;
        
        @JsonProperty("productName")
        private String productName;
        
        @JsonProperty("returnRate")
        private Double returnRate;
        
        @JsonProperty("totalReturns")
        private Long totalReturns;
        
        public ProductReturnRate() {}
        
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public Double getReturnRate() {
            return returnRate;
        }
        
        public void setReturnRate(Double returnRate) {
            this.returnRate = returnRate;
        }
        
        public Long getTotalReturns() {
            return totalReturns;
        }
        
        public void setTotalReturns(Long totalReturns) {
            this.totalReturns = totalReturns;
        }
    }
    
    /**
     * Inner class for monthly return statistics
     */
    public static class MonthlyReturnStats {
        @JsonProperty("month")
        private String month;
        
        @JsonProperty("year")
        private Integer year;
        
        @JsonProperty("totalReturns")
        private Long totalReturns;
        
        @JsonProperty("totalValue")
        private Double totalValue;
        
        public MonthlyReturnStats() {}
        
        public String getMonth() {
            return month;
        }
        
        public void setMonth(String month) {
            this.month = month;
        }
        
        public Integer getYear() {
            return year;
        }
        
        public void setYear(Integer year) {
            this.year = year;
        }
        
        public Long getTotalReturns() {
            return totalReturns;
        }
        
        public void setTotalReturns(Long totalReturns) {
            this.totalReturns = totalReturns;
        }
        
        public Double getTotalValue() {
            return totalValue;
        }
        
        public void setTotalValue(Double totalValue) {
            this.totalValue = totalValue;
        }
    }
}

