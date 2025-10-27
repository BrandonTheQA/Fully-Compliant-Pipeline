package com.example.controller;

import com.example.dto.CreateUserRequest;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserResponse;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for user management endpoints
 */
@RestController
@RequestMapping("/api")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * POST /api/users - Create a new user
     */
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * GET /api/users/{id} - Get user profile by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        UserResponse user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * POST /api/login - Authenticate user
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
