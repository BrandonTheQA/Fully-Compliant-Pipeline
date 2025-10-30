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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService();
        try {
            java.lang.reflect.Field field = UserService.class.getDeclaredField("userRepository");
            field.setAccessible(true);
            field.set(userService, userRepository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject repository", e);
        }
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        CreateUserRequest req = new CreateUserRequest("John Doe", "john@example.com", "secret123");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.createUser(req);

        assertNotNull(resp);
        assertEquals("John Doe", resp.getName());
        assertEquals("john@example.com", resp.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should not create user if email exists")
    void shouldNotCreateUserIfEmailExists() {
        CreateUserRequest req = new CreateUserRequest("John Doe", "john@example.com", "secret123");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(req));
    }

    @Test
    @DisplayName("Should get user by id")
    void shouldGetUserById() {
        User user = new User("user-id", "John Doe", "john@example.com", "secret");
        when(userRepository.findById("user-id")).thenReturn(Optional.of(user));

        UserResponse resp = userService.getUser("user-id");
        assertEquals("user-id", resp.getUserId());
        verify(userRepository).findById("user-id");
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUser("missing"));
    }

    @Test
    @DisplayName("Should authenticate successfully")
    void shouldAuthenticateSuccessfully() {
        User user = new User("user-id", "John Doe", "john@example.com", "secret");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        LoginResponse resp = userService.authenticate(new LoginRequest("john@example.com", "secret"));
        assertEquals("user-id", resp.getUserId());
        assertNotNull(resp.getToken());
    }

    @Test
    @DisplayName("Should fail authentication for wrong password")
    void shouldFailAuthenticationForWrongPassword() {
        User user = new User("user-id", "John Doe", "john@example.com", "secret");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        assertThrows(AuthenticationException.class, () -> userService.authenticate(new LoginRequest("john@example.com", "bad")));
    }
}
