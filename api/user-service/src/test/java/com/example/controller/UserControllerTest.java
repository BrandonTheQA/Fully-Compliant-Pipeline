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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("UserController Tests")
class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController userController = new UserController();
        try {
            java.lang.reflect.Field field = UserController.class.getDeclaredField("userService");
            field.setAccessible(true);
            field.set(userController, userService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject service", e);
        }

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.example.exception.GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        CreateUserRequest req = new CreateUserRequest("John Doe", "john@example.com", "secret123");
        UserResponse resp = new UserResponse("user-id", "John Doe", "john@example.com", "2023-01-01T00:00:00");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("Should get user successfully")
    void shouldGetUserSuccessfully() throws Exception {
        UserResponse resp = new UserResponse("user-id", "John Doe", "john@example.com", "2023-01-01T00:00:00");
        when(userService.getUser("user-id")).thenReturn(resp);

        mockMvc.perform(get("/api/users/user-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("Should handle user not found")
    void shouldHandleUserNotFound() throws Exception {
        when(userService.getUser("missing")).thenThrow(new UserNotFoundException("User not found"));
        mockMvc.perform(get("/api/users/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("Should handle user already exists on create")
    void shouldHandleUserAlreadyExistsOnCreate() throws Exception {
        CreateUserRequest req = new CreateUserRequest("John Doe", "john@example.com", "secret123");
        when(userService.createUser(any(CreateUserRequest.class))).thenThrow(new UserAlreadyExistsException("exists"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    @Test
    @DisplayName("Should authenticate successfully")
    void shouldAuthenticateSuccessfully() throws Exception {
        LoginRequest req = new LoginRequest("john@example.com", "secret123");
        LoginResponse resp = new LoginResponse("Login successful", "token", "user-id", "John", "john@example.com", "2023-01-01T00:00:00");
        when(userService.authenticate(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.userId").value("user-id"));
    }

    @Test
    @DisplayName("Should handle invalid credentials")
    void shouldHandleInvalidCredentials() throws Exception {
        LoginRequest req = new LoginRequest("john@example.com", "wrong");
        when(userService.authenticate(any(LoginRequest.class))).thenThrow(new AuthenticationException("Invalid email or password"));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"));
    }

    @Test
    @DisplayName("Should validate create user request")
    void shouldValidateCreateUserRequest() throws Exception {
        CreateUserRequest invalid = new CreateUserRequest("", "bademail", "123");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
