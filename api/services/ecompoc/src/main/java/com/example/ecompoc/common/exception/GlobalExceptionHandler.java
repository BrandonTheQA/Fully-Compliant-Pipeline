package com.example.ecompoc.common.exception;

import com.example.ecompoc.order.exception.OrderNotFoundException;
import com.example.ecompoc.order.exception.OrderValidationException;
import com.example.ecompoc.order.exception.ServiceUnavailableException;
import com.example.ecompoc.product.exception.ProductAlreadyExistsException;
import com.example.ecompoc.product.exception.ProductNotFoundException;
import com.example.ecompoc.user.exception.AuthenticationException;
import com.example.ecompoc.user.exception.UserAlreadyExistsException;
import com.example.ecompoc.user.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("message", ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toArray());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
        UserAlreadyExistsException.class,
        ProductAlreadyExistsException.class,
        OrderValidationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleBusinessValidation(RuntimeException ex) {
        logger.warn("Business validation failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        logger.debug("IllegalArgumentException stack trace:", ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({
        UserNotFoundException.class,
        ProductNotFoundException.class,
        OrderNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(ServiceUnavailableException ex) {
        logger.error("Dependent service unavailable: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        logger.error("Exception class: {}", ex.getClass().getName());
        logger.error("Stack trace:", ex);
        if (ex.getCause() != null) {
            logger.error("Caused by: {}", ex.getCause().getMessage(), ex.getCause());
        }
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
            "Internal server error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}

