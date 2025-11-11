package com.example.ecompoc.user.controller;

import com.example.ecompoc.common.exception.GlobalExceptionHandler;
import com.example.ecompoc.user.dto.CreateUserRequest;
import com.example.ecompoc.user.dto.LoginRequest;
import com.example.ecompoc.user.dto.LoginResponse;
import com.example.ecompoc.user.dto.UserResponse;
import com.example.ecompoc.user.exception.AuthenticationException;
import com.example.ecompoc.user.exception.UserAlreadyExistsException;
import com.example.ecompoc.user.exception.UserNotFoundException;
import com.example.ecompoc.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 */
@DisplayName("UserController Tests")
class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ObjectMapper objectMapper;
    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController userController = new UserController(userService);

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
        
        // Setup test data
        createUserRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
        userResponse = new UserResponse("user-id", "John Doe", "john@example.com", "2023-01-01T00:00:00");
        loginResponse = new LoginResponse("Login successful", "token123", "user-id", "John Doe", "john@example.com", "2023-01-01T00:00:00");
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.createdAt").value("2023-01-01T00:00:00"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should get user successfully")
    void shouldGetUserSuccessfully() throws Exception {
        // Given
        when(userService.getUser("user-id")).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/user-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.createdAt").value("2023-01-01T00:00:00"));

        verify(userService).getUser("user-id");
    }

    @Test
    @DisplayName("Should login user successfully")
    void shouldLoginUserSuccessfully() throws Exception {
        // Given
        when(userService.authenticate(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.loginTime").value("2023-01-01T00:00:00"));

        verify(userService).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should handle UserAlreadyExistsException")
    void shouldHandleUserAlreadyExistsException() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email john@example.com already exists"));
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User with email john@example.com already exists"));
        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should handle UserNotFoundException")
    void shouldHandleUserNotFoundException() throws Exception {
        when(userService.getUser("non-existent-id"))
                .thenThrow(new UserNotFoundException("User not found"));
        mockMvc.perform(get("/api/users/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
        verify(userService).getUser("non-existent-id");
    }

    @Test
    @DisplayName("Should handle AuthenticationException")
    void shouldHandleAuthenticationException() throws Exception {
        when(userService.authenticate(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid email or password"));
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
        verify(userService).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for create user")
    void shouldHandleValidationErrorsForCreateUser() throws Exception {
        // Given - invalid request with missing required fields
        CreateUserRequest invalidRequest = new CreateUserRequest("", "", "");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for login")
    void shouldHandleValidationErrorsForLogin() throws Exception {
        // Given - invalid request with missing required fields
        LoginRequest invalidRequest = new LoginRequest("", "");

        // When & Then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should handle null path variable")
    void shouldHandleNullPathVariable() throws Exception {
        // Given
        when(userService.getUser("null")).thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/null"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(userService).getUser("null");
    }

    @Test
    @DisplayName("Should handle unsupported media type")
    void shouldHandleUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }
}