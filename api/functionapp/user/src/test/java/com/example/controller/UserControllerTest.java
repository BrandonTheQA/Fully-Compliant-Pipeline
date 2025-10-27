package com.example.controller;

import com.example.dto.CreateUserRequest;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserResponse;
import com.example.exception.AuthenticationException;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.example.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        createUserRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
        userResponse = new UserResponse("user-123", "John Doe", "john@example.com", "2023-01-01T10:00:00");
        loginResponse = new LoginResponse("Login successful", "token-123", "user-123", "John Doe", "john@example.com", "2023-01-01T10:00:00");
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.createdAt").value("2023-01-01T10:00:00"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should return 201 status when creating user")
    void shouldReturn201StatusWhenCreatingUser() {
        // Given
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.createUser(createUserRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("user-123", response.getBody().getUserId());
    }

    @Test
    @DisplayName("Should get user successfully")
    void shouldGetUserSuccessfully() throws Exception {
        // Given
        when(userService.getUser("user-123")).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/user-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getUser("user-123");
    }

    @Test
    @DisplayName("Should return 200 status when getting user")
    void shouldReturn200StatusWhenGettingUser() {
        // Given
        when(userService.getUser("user-123")).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.getUser("user-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("user-123", response.getBody().getUserId());
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        when(userService.authenticate(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 200 status when login is successful")
    void shouldReturn200StatusWhenLoginIsSuccessful() {
        // Given
        when(userService.authenticate(any(LoginRequest.class))).thenReturn(loginResponse);

        // When
        ResponseEntity<LoginResponse> response = userController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle UserAlreadyExistsException")
    void shouldHandleUserAlreadyExistsException() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email john@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists"))
                .andExpect(jsonPath("$.message").value("User with email john@example.com already exists"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should handle UserNotFoundException")
    void shouldHandleUserNotFoundException() throws Exception {
        // Given
        when(userService.getUser("non-existent-id"))
                .thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).getUser("non-existent-id");
    }

    @Test
    @DisplayName("Should handle AuthenticationException")
    void shouldHandleAuthenticationException() throws Exception {
        // Given
        when(userService.authenticate(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid email or password"));

        // When & Then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(userService).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should validate request body for create user")
    void shouldValidateRequestBodyForCreateUser() throws Exception {
        // Given - Invalid request with missing name
        CreateUserRequest invalidRequest = new CreateUserRequest("", "john@example.com", "password123");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should validate request body for login")
    void shouldValidateRequestBodyForLogin() throws Exception {
        // Given - Invalid request with missing email
        LoginRequest invalidRequest = new LoginRequest("", "password123");

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
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).getUser("null");
    }

    @Test
    @DisplayName("Should handle service returning null")
    void shouldHandleServiceReturningNull() {
        // Given
        when(userService.getUser("user-123")).thenReturn(null);

        // When
        ResponseEntity<UserResponse> response = userController.getUser("user-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
}
