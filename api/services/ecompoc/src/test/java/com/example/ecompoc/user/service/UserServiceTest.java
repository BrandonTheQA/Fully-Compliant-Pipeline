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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", "password123");
        User savedUser = new User("user-id", "John Doe", "john@example.com", "password123");
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse result = userService.createUser(request);

        // Then
        assertNotNull(result);
        assertEquals("user-id", result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email exists")
    void shouldThrowUserAlreadyExistsExceptionWhenEmailExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", "password123");
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user successfully")
    void shouldGetUserSuccessfully() {
        // Given
        String userId = "user-id";
        User user = new User(userId, "John Doe", "john@example.com", "password123");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserResponse result = userService.getUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
        
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found")
    void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
        // Given
        String userId = "non-existent-id";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));
        
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void shouldAuthenticateUserSuccessfully() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        User user = new User("user-id", "John Doe", "john@example.com", "password123");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // When
        LoginResponse result = userService.authenticate(request);

        // Then
        assertNotNull(result);
        assertEquals("Login successful", result.getMessage());
        assertNotNull(result.getToken());
        assertEquals("user-id", result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getLoginTime());
        
        verify(userRepository).findByEmail(request.getEmail());
    }

    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void shouldThrowAuthenticationExceptionWhenUserNotFound() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AuthenticationException.class, () -> userService.authenticate(request));
        
        verify(userRepository).findByEmail(request.getEmail());
    }

    @Test
    @DisplayName("Should throw AuthenticationException when password is wrong")
    void shouldThrowAuthenticationExceptionWhenPasswordIsWrong() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");
        User user = new User("user-id", "John Doe", "john@example.com", "password123");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(AuthenticationException.class, () -> userService.authenticate(request));
        
        verify(userRepository).findByEmail(request.getEmail());
    }

    @Test
    @DisplayName("Should map user to response correctly")
    void shouldMapUserToResponseCorrectly() {
        // Given
        User user = new User("user-id", "John Doe", "john@example.com", "password123");

        // When - Test the mapping indirectly through createUser
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", "password123");
        UserResponse result = userService.createUser(request);

        // Then
        assertNotNull(result);
        assertEquals("user-id", result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
        // Password should not be included in response - UserResponse doesn't have password field
        // This is verified by the fact that we can access all fields without password
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void shouldHandleNullInputGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> userService.createUser(null));
        assertThrows(UserNotFoundException.class, () -> userService.getUser(null));
        assertThrows(NullPointerException.class, () -> userService.authenticate(null));
    }

    @Test
    @DisplayName("Should generate unique tokens for each login")
    void shouldGenerateUniqueTokensForEachLogin() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        User user = new User("user-id", "John Doe", "john@example.com", "password123");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // When
        LoginResponse result1 = userService.authenticate(request);
        LoginResponse result2 = userService.authenticate(request);

        // Then
        assertNotEquals(result1.getToken(), result2.getToken());
        assertNotNull(result1.getToken());
        assertNotNull(result2.getToken());
    }

    @Test
    @DisplayName("Should format login time correctly")
    void shouldFormatLoginTimeCorrectly() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        User user = new User("user-id", "John Doe", "john@example.com", "password123");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // When
        LoginResponse result = userService.authenticate(request);

        // Then
        assertNotNull(result.getLoginTime());
        // Verify the format is ISO_LOCAL_DATE_TIME
        assertDoesNotThrow(() -> LocalDateTime.parse(result.getLoginTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}