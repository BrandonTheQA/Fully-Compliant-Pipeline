package com.example.ecompoc.order.exception;

/**
 * Exception thrown when order validation fails
 */
public class OrderValidationException extends RuntimeException {
    
    public OrderValidationException(String message) {
        super(message);
    }
}

