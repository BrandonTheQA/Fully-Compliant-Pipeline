package com.example.ecompoc.user.service;

import com.example.ecompoc.user.dto.CreateUserRequest;
import com.example.ecompoc.user.dto.LoginRequest;
import com.example.ecompoc.user.dto.LoginResponse;
import com.example.ecompoc.user.dto.UserResponse;
import com.example.ecompoc.user.exception.AuthenticationException;
import com.example.ecompoc.user.exception.UserAlreadyExistsException;
import com.example.ecompoc.user.exception.UserNotFoundException;
import com.example.ecompoc.user.model.User;
import com.example.ecompoc.user.repository.UserRepository;
import com.example.ecompoc.loyalty.service.LoyaltyService;
import com.example.ecompoc.loyalty.model.EnrollmentSource;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LoyaltyService loyaltyService;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Autowired(required = false)
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
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
        
        // Auto-enroll in loyalty program
        if (loyaltyService != null) {
            try {
                // Check if CreateUserRequest has referralCode field (optional)
                String referralCode = null;
                // Note: If CreateUserRequest is extended with referralCode, use it here
                loyaltyService.enrollUser(userId, EnrollmentSource.AUTO, referralCode);
                // Log success but don't fail user creation if enrollment fails
            } catch (Exception e) {
                // Log but don't fail user creation if loyalty enrollment fails
                // This ensures user creation succeeds even if loyalty service is unavailable
            }
        }
        
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
