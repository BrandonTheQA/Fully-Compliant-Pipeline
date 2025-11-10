package com.example.monolith.product.exception;

/**
 * Exception thrown when a product already exists
 */
public class ProductAlreadyExistsException extends RuntimeException {
    
    public ProductAlreadyExistsException(String message) {
        super(message);
    }
    
    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

