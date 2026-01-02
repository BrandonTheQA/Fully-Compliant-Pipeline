package com.example.ecompoc.returns.dto;

import com.example.ecompoc.returns.enums.ReturnReason;
import com.example.ecompoc.returns.enums.ReturnType;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for creating a return request
 */
public class CreateReturnRequest {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Return must contain at least one item")
    @Valid
    private List<ReturnItemRequest> items;
    
    @NotBlank(message = "Return type is required")
    private String returnType; // REFUND_TO_PAYMENT, STORE_CREDIT, EXCHANGE
    
    private String comments;
    
    // Default constructor
    public CreateReturnRequest() {}
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<ReturnItemRequest> getItems() {
        return items;
    }
    
    public void setItems(List<ReturnItemRequest> items) {
        this.items = items;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    /**
     * Inner class for return item request
     */
    public static class ReturnItemRequest {
        @javax.validation.constraints.NotNull(message = "Order item ID is required")
        private Long orderItemId;
        
        @javax.validation.constraints.Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @NotBlank(message = "Return reason is required")
        private String returnReason; // DEFECTIVE, WRONG_ITEM, etc.
        
        private String condition;
        
        private String comments;
        
        public ReturnItemRequest() {}
        
        public ReturnItemRequest(Long orderItemId, Integer quantity, String returnReason) {
            this.orderItemId = orderItemId;
            this.quantity = quantity;
            this.returnReason = returnReason;
        }
        
        public Long getOrderItemId() {
            return orderItemId;
        }
        
        public void setOrderItemId(Long orderItemId) {
            this.orderItemId = orderItemId;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public String getReturnReason() {
            return returnReason;
        }
        
        public void setReturnReason(String returnReason) {
            this.returnReason = returnReason;
        }
        
        public String getCondition() {
            return condition;
        }
        
        public void setCondition(String condition) {
            this.condition = condition;
        }
        
        public String getComments() {
            return comments;
        }
        
        public void setComments(String comments) {
            this.comments = comments;
        }
    }
}

