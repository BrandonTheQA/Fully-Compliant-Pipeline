package com.example.ecompoc.user.integration;

import com.example.ecompoc.user.dto.CreateUserRequest;
import com.example.ecompoc.user.dto.LoginRequest;
import com.example.ecompoc.user.dto.LoginResponse;
import com.example.ecompoc.user.dto.UserResponse;
import com.example.ecompoc.user.exception.AuthenticationException;
import com.example.ecompoc.user.exception.UserAlreadyExistsException;
import com.example.ecompoc.user.exception.UserNotFoundException;
import com.example.ecompoc.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete user management flow
 */
@DisplayName("User Management Integration Tests")
@SpringBootTest(properties = {"spring.liquibase.enabled=false"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserManagementIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Should complete user registration and login flow")
    void shouldCompleteUserRegistrationAndLoginFlow() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");
        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");

        // When - Create user
        UserResponse userResponse = userService.createUser(createRequest);
        
        // Then - Verify user creation
        assertNotNull(userResponse);
        assertEquals("John Doe", userResponse.getName());
        assertEquals("john@example.com", userResponse.getEmail());
        assertNotNull(userResponse.getUserId());
        assertNotNull(userResponse.getCreatedAt());

        // When - Login user
        LoginResponse loginResponse = userService.authenticate(loginRequest);
        
        // Then - Verify login
        assertEquals("Login successful", loginResponse.getMessage());
        assertNotNull(loginResponse.getToken());
        assertEquals(userResponse.getUserId(), loginResponse.getUserId());
        assertEquals("John Doe", loginResponse.getName());
        assertEquals("john@example.com", loginResponse.getEmail());
        assertNotNull(loginResponse.getLoginTime());

        // When - Get user profile
        UserResponse retrievedUser = userService.getUser(userResponse.getUserId());
        
        // Then - Verify profile retrieval
        assertEquals(userResponse.getUserId(), retrievedUser.getUserId());
        assertEquals("John Doe", retrievedUser.getName());
        assertEquals("john@example.com", retrievedUser.getEmail());
        assertNotNull(retrievedUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle duplicate user registration")
    void shouldHandleDuplicateUserRegistration() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");

        // When - Create first user
        userService.createUser(createRequest);

        // When & Then - Try to create duplicate user
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createRequest));
    }

    @Test
    @DisplayName("Should handle invalid login credentials")
    void shouldHandleInvalidLoginCredentials() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");
        LoginRequest invalidLoginRequest = new LoginRequest("john@example.com", "wrongpassword");

        // When - Create user
        userService.createUser(createRequest);

        // When & Then - Try invalid login
        assertThrows(AuthenticationException.class, () -> userService.authenticate(invalidLoginRequest));
    }

    @Test
    @DisplayName("Should handle non-existent user login")
    void shouldHandleNonExistentUserLogin() {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");

        // When & Then - Try login with non-existent user
        assertThrows(AuthenticationException.class, () -> userService.authenticate(loginRequest));
    }

    @Test
    @DisplayName("Should handle non-existent user profile retrieval")
    void shouldHandleNonExistentUserProfileRetrieval() {
        // When & Then - Try to get non-existent user
        assertThrows(UserNotFoundException.class, () -> userService.getUser("non-existent-id"));
    }

    @Test
    @DisplayName("Should handle multiple users independently")
    void shouldHandleMultipleUsersIndependently() {
        // Given
        CreateUserRequest user1Request = new CreateUserRequest("John Doe", "john@example.com", "password123");
        CreateUserRequest user2Request = new CreateUserRequest("Jane Smith", "jane@example.com", "password456");
        LoginRequest user1LoginRequest = new LoginRequest("john@example.com", "password123");
        LoginRequest user2LoginRequest = new LoginRequest("jane@example.com", "password456");

        // When - Create both users
        UserResponse user1 = userService.createUser(user1Request);
        UserResponse user2 = userService.createUser(user2Request);

        // Then - Verify both users were created
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotEquals(user1.getUserId(), user2.getUserId());

        // When - Login both users
        LoginResponse user1Login = userService.authenticate(user1LoginRequest);
        LoginResponse user2Login = userService.authenticate(user2LoginRequest);

        // Then - Verify both logins succeeded
        assertEquals(user1.getUserId(), user1Login.getUserId());
        assertEquals(user2.getUserId(), user2Login.getUserId());

        // When - Get both user profiles
        UserResponse retrievedUser1 = userService.getUser(user1.getUserId());
        UserResponse retrievedUser2 = userService.getUser(user2.getUserId());

        // Then - Verify both profiles are correct
        assertEquals("John Doe", retrievedUser1.getName());
        assertEquals("Jane Smith", retrievedUser2.getName());
    }
}
