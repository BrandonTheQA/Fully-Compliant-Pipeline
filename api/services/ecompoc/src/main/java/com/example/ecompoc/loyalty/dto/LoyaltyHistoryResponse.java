package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for paginated loyalty transaction history
 */
public class LoyaltyHistoryResponse {
    
    @JsonProperty("transactions")
    private List<LoyaltyTransactionResponse> transactions;
    
    @JsonProperty("totalElements")
    private Long totalElements;
    
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @JsonProperty("currentPage")
    private Integer currentPage;
    
    @JsonProperty("pageSize")
    private Integer pageSize;
    
    // Default constructor
    public LoyaltyHistoryResponse() {}
    
    // Getters and Setters
    public List<LoyaltyTransactionResponse> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<LoyaltyTransactionResponse> transactions) {
        this.transactions = transactions;
    }
    
    public Long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Integer getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
