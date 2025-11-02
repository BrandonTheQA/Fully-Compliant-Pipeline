package com.example.exception;

/**
 * Exception thrown when order validation fails
 */
public class OrderValidationException extends RuntimeException {
    
    public OrderValidationException(String message) {
        super(message);
    }
}

