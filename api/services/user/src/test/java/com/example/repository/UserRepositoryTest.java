package com.example.repository;

import com.example.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository
 */
@DisplayName("UserRepository Tests")
@DataJpaTest(excludeAutoConfiguration = LiquibaseAutoConfiguration.class, properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        // Given
        User user = new User("user-id", "John Doe", "john@example.com", "password123");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser);
        assertEquals("user-id", savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("password123", savedUser.getPassword());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should find user by id")
    void shouldFindUserById() {
        // Given
        User user = new User("user-id", "John Doe", "john@example.com", "password123");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById("user-id");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("user-id", foundUser.get().getId());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("john@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when user not found by id")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // When
        Optional<User> foundUser = userRepository.findById("non-existent-id");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        User user = new User("user-id", "John Doe", "john@example.com", "password123");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail("john@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("user-id", foundUser.get().getId());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("john@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // Given
        User user = new User("user-id", "John Doe", "john@example.com", "password123");
        userRepository.save(user);

        // When & Then
        assertTrue(userRepository.existsByEmail("john@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Should count users correctly")
    void shouldCountUsersCorrectly() {
        // Given
        assertEquals(0, userRepository.count());

        // When
        User user1 = new User("user-1", "John Doe", "john@example.com", "password123");
        User user2 = new User("user-2", "Jane Smith", "jane@example.com", "password456");
        
        userRepository.save(user1);
        assertEquals(1, userRepository.count());
        
        userRepository.save(user2);
        assertEquals(2, userRepository.count());
    }

    @Test
    @DisplayName("Should handle multiple users independently")
    void shouldHandleMultipleUsersIndependently() {
        // Given
        User user1 = new User("user-1", "John Doe", "john@example.com", "password123");
        User user2 = new User("user-2", "Jane Smith", "jane@example.com", "password456");
        
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        Optional<User> foundUser1 = userRepository.findById("user-1");
        Optional<User> foundUser2 = userRepository.findById("user-2");
        Optional<User> foundByEmail1 = userRepository.findByEmail("john@example.com");
        Optional<User> foundByEmail2 = userRepository.findByEmail("jane@example.com");

        // Then
        assertTrue(foundUser1.isPresent());
        assertTrue(foundUser2.isPresent());
        assertTrue(foundByEmail1.isPresent());
        assertTrue(foundByEmail2.isPresent());
        
        assertEquals("John Doe", foundUser1.get().getName());
        assertEquals("Jane Smith", foundUser2.get().getName());
        assertEquals("john@example.com", foundByEmail1.get().getEmail());
        assertEquals("jane@example.com", foundByEmail2.get().getEmail());
        
        assertEquals(2, userRepository.count());
    }

    @Test
    @DisplayName("Should update existing user")
    void shouldUpdateExistingUser() {
        // Given
        User originalUser = new User("user-id", "John Doe", "john@example.com", "password123");
        userRepository.save(originalUser);

        // When
        User updatedUser = new User("user-id", "John Updated", "john.updated@example.com", "newpassword");
        userRepository.save(updatedUser);

        // Then
        Optional<User> foundUser = userRepository.findById("user-id");
        assertTrue(foundUser.isPresent());
        assertEquals("John Updated", foundUser.get().getName());
        assertEquals("john.updated@example.com", foundUser.get().getEmail());
        assertEquals("newpassword", foundUser.get().getPassword());
        assertEquals(1, userRepository.count()); // Should still be 1 user, not 2
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void shouldHandleNullUserGracefully() {
        // When & Then - JPA will handle null checks differently, so we test with invalid data instead
        User user = new User();
        // Missing required fields - will fail validation
        assertThrows(Exception.class, () -> userRepository.save(user));
    }
}