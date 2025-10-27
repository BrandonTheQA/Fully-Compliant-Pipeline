package com.example.service;

import com.example.dto.CreateUserRequest;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserResponse;
import com.example.exception.AuthenticationException;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
        testUser = new User("user-123", "John Doe", "john@example.com", "password123");
    }

    @Test
    @DisplayName("Should create user successfully when email does not exist")
    void shouldCreateUserSuccessfullyWhenEmailDoesNotExist() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.createUser(createUserRequest);

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email already exists")
    void shouldThrowUserAlreadyExistsExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> userService.createUser(createUserRequest)
        );

        assertEquals("User with email john@example.com already exists", exception.getMessage());
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user successfully when user exists")
    void shouldGetUserSuccessfullyWhenUserExists() {
        // Given
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUser("user-123");

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());

        verify(userRepository).findById("user-123");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Given
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.getUser("non-existent-id")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById("non-existent-id");
    }

    @Test
    @DisplayName("Should authenticate user successfully with valid credentials")
    void shouldAuthenticateUserSuccessfullyWithValidCredentials() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When
        LoginResponse result = userService.authenticate(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("Login successful", result.getMessage());
        assertNotNull(result.getToken());
        assertEquals("user-123", result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getLoginTime());

        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when email does not exist")
    void shouldThrowAuthenticationExceptionWhenEmailDoesNotExist() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        LoginRequest invalidRequest = new LoginRequest("nonexistent@example.com", "password123");
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> userService.authenticate(invalidRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when password is incorrect")
    void shouldThrowAuthenticationExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        LoginRequest invalidRequest = new LoginRequest("john@example.com", "wrongpassword");

        // When & Then
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> userService.authenticate(invalidRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should generate unique tokens for different login attempts")
    void shouldGenerateUniqueTokensForDifferentLoginAttempts() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When
        LoginResponse result1 = userService.authenticate(loginRequest);
        LoginResponse result2 = userService.authenticate(loginRequest);

        // Then
        assertNotEquals(result1.getToken(), result2.getToken());
        assertNotNull(result1.getToken());
        assertNotNull(result2.getToken());
    }

    @Test
    @DisplayName("Should map user to response correctly")
    void shouldMapUserToResponseCorrectly() {
        // Given
        User user = new User("test-id", "Test User", "test@example.com", "password");
        when(userRepository.findById("test-id")).thenReturn(Optional.of(user));

        // When
        UserResponse result = userService.getUser("test-id");

        // Then
        assertEquals("test-id", result.getUserId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
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
    @DisplayName("Should handle empty string input gracefully")
    void shouldHandleEmptyStringInputGracefully() {
        // Given
        when(userRepository.findById("")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUser(""));
        verify(userRepository).findById("");
    }
}
