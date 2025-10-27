package com.example.repository;

import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository
 */
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        // Given
        User user = new User("user-123", "John Doe", "john@example.com", "password123");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser);
        assertEquals("user-123", savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("password123", savedUser.getPassword());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should find user by ID when user exists")
    void shouldFindUserByIdWhenUserExists() {
        // Given
        User user = new User("user-123", "John Doe", "john@example.com", "password123");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById("user-123");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("user-123", foundUser.get().getId());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("john@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty optional when user does not exist")
    void shouldReturnEmptyOptionalWhenUserDoesNotExist() {
        // When
        Optional<User> foundUser = userRepository.findById("non-existent-id");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should find user by email when user exists")
    void shouldFindUserByEmailWhenUserExists() {
        // Given
        User user = new User("user-123", "John Doe", "john@example.com", "password123");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail("john@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("user-123", foundUser.get().getId());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("john@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty optional when email does not exist")
    void shouldReturnEmptyOptionalWhenEmailDoesNotExist() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should return true when user exists by email")
    void shouldReturnTrueWhenUserExistsByEmail() {
        // Given
        User user = new User("user-123", "John Doe", "john@example.com", "password123");
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("john@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when user does not exist by email")
    void shouldReturnFalseWhenUserDoesNotExistByEmail() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should return correct count of users")
    void shouldReturnCorrectCountOfUsers() {
        // Given
        assertEquals(0, userRepository.count());

        // When
        userRepository.save(new User("user-1", "John Doe", "john@example.com", "password123"));
        userRepository.save(new User("user-2", "Jane Smith", "jane@example.com", "password456"));

        // Then
        assertEquals(2, userRepository.count());
    }

    @Test
    @DisplayName("Should handle multiple users with different emails")
    void shouldHandleMultipleUsersWithDifferentEmails() {
        // Given
        User user1 = new User("user-1", "John Doe", "john@example.com", "password123");
        User user2 = new User("user-2", "Jane Smith", "jane@example.com", "password456");
        
        userRepository.save(user1);
        userRepository.save(user2);

        // When & Then
        assertTrue(userRepository.existsByEmail("john@example.com"));
        assertTrue(userRepository.existsByEmail("jane@example.com"));
        assertFalse(userRepository.existsByEmail("bob@example.com"));

        Optional<User> foundUser1 = userRepository.findByEmail("john@example.com");
        Optional<User> foundUser2 = userRepository.findByEmail("jane@example.com");

        assertTrue(foundUser1.isPresent());
        assertTrue(foundUser2.isPresent());
        assertEquals("user-1", foundUser1.get().getId());
        assertEquals("user-2", foundUser2.get().getId());
    }

    @Test
    @DisplayName("Should update existing user when saving with same ID")
    void shouldUpdateExistingUserWhenSavingWithSameId() {
        // Given
        User originalUser = new User("user-123", "John Doe", "john@example.com", "password123");
        userRepository.save(originalUser);

        // When
        User updatedUser = new User("user-123", "John Updated", "john.updated@example.com", "newpassword");
        userRepository.save(updatedUser);

        // Then
        Optional<User> foundUser = userRepository.findById("user-123");
        assertTrue(foundUser.isPresent());
        assertEquals("John Updated", foundUser.get().getName());
        assertEquals("john.updated@example.com", foundUser.get().getEmail());
        assertEquals("newpassword", foundUser.get().getPassword());
        assertEquals(1, userRepository.count()); // Should still be 1 user
    }
}
