package com.example.repository;

import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() { userRepository = new UserRepository(); }

    @Test
    @DisplayName("Should save and find user")
    void shouldSaveAndFindUser() {
        User user = new User("user-id", "John Doe", "john@example.com", "secret");
        userRepository.save(user);
        Optional<User> found = userRepository.findById("user-id");
        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should find by email and check existence")
    void shouldFindByEmailAndCheckExistence() {
        User user = new User("user-id", "John Doe", "john@example.com", "secret");
        userRepository.save(user);
        assertTrue(userRepository.findByEmail("john@example.com").isPresent());
        assertTrue(userRepository.existsByEmail("john@example.com"));
        assertFalse(userRepository.existsByEmail("jane@example.com"));
    }
}
