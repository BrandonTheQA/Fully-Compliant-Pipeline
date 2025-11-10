package com.example.monolith.user.service;

import com.example.monolith.user.dto.CreateUserRequest;
import com.example.monolith.user.dto.LoginRequest;
import com.example.monolith.user.dto.LoginResponse;
import com.example.monolith.user.dto.UserResponse;
import com.example.monolith.user.exception.AuthenticationException;
import com.example.monolith.user.exception.UserAlreadyExistsException;
import com.example.monolith.user.exception.UserNotFoundException;
import com.example.monolith.user.model.User;
import com.example.monolith.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service layer for user management
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Create a new user
     */
    public UserResponse createUser(CreateUserRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        // Create new user
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, request.getName(), request.getEmail(), request.getPassword());
        User savedUser = userRepository.save(user);
        
        return mapToResponse(savedUser);
    }
    
    /**
     * Get user by ID
     */
    public UserResponse getUser(String id) {
        if (id == null || id.isBlank()) {
            throw new UserNotFoundException("User not found");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        return mapToResponse(user);
    }
    
    /**
     * Authenticate user
     */
    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
        
        if (!user.getPassword().equals(request.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }
        
        // Generate simple token (in production, use proper JWT)
        String token = UUID.randomUUID().toString();
        String loginTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        return new LoginResponse(
            "Login successful",
            token,
            user.getId(),
            user.getName(),
            user.getEmail(),
            loginTime
        );
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        String createdAtStr = user.getCreatedAt() != null 
            ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : null;
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            createdAtStr
        );
    }
}
