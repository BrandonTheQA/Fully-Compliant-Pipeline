package com.example.ecompoc.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordEncoder (BCrypt) functionality
 */
@DisplayName("PasswordEncoder Tests")
class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
    }

    @Test
    @DisplayName("Should encode password and produce BCrypt hash format")
    void shouldEncodePasswordAndProduceBCryptHashFormat() {
        // Given
        String plainPassword = "SecurePassword123";

        // When
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // Then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$"),
                "Password hash should start with BCrypt identifier");
        assertEquals(60, hashedPassword.length(), "BCrypt hash should be 60 characters long");
        assertNotEquals(plainPassword, hashedPassword, "Hashed password should not match plaintext");
    }

    @Test
    @DisplayName("Should match correct password with hash")
    void shouldMatchCorrectPasswordWithHash() {
        // Given
        String plainPassword = "SecurePassword123";
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // When
        boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);

        // Then
        assertTrue(matches, "PasswordEncoder should match correct password with hash");
    }

    @Test
    @DisplayName("Should not match incorrect password with hash")
    void shouldNotMatchIncorrectPasswordWithHash() {
        // Given
        String plainPassword = "SecurePassword123";
        String wrongPassword = "WrongPassword456";
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // When
        boolean matches = passwordEncoder.matches(wrongPassword, hashedPassword);

        // Then
        assertFalse(matches, "PasswordEncoder should not match incorrect password with hash");
    }

    @Test
    @DisplayName("Should produce different hashes for same password (salt uniqueness)")
    void shouldProduceDifferentHashesForSamePassword() {
        // Given
        String plainPassword = "SecurePassword123";

        // When
        String hash1 = passwordEncoder.encode(plainPassword);
        String hash2 = passwordEncoder.encode(plainPassword);

        // Then
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to unique salts");
        
        // Both hashes should still match the original password
        assertTrue(passwordEncoder.matches(plainPassword, hash1), "First hash should match password");
        assertTrue(passwordEncoder.matches(plainPassword, hash2), "Second hash should match password");
    }

    @Test
    @DisplayName("Should produce different hashes for different passwords")
    void shouldProduceDifferentHashesForDifferentPasswords() {
        // Given
        String password1 = "Password123";
        String password2 = "Password456";

        // When
        String hash1 = passwordEncoder.encode(password1);
        String hash2 = passwordEncoder.encode(password2);

        // Then
        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
        
        // Verify each hash only matches its own password
        assertTrue(passwordEncoder.matches(password1, hash1), "Hash1 should match password1");
        assertFalse(passwordEncoder.matches(password2, hash1), "Hash1 should not match password2");
        assertTrue(passwordEncoder.matches(password2, hash2), "Hash2 should match password2");
        assertFalse(passwordEncoder.matches(password1, hash2), "Hash2 should not match password1");
    }

    @Test
    @DisplayName("Should handle case-sensitive passwords")
    void shouldHandleCaseSensitivePasswords() {
        // Given
        String password = "Password123";
        String passwordLowercase = "password123";
        String hashedPassword = passwordEncoder.encode(password);

        // When
        boolean matchesCorrect = passwordEncoder.matches(password, hashedPassword);
        boolean matchesIncorrect = passwordEncoder.matches(passwordLowercase, hashedPassword);

        // Then
        assertTrue(matchesCorrect, "BCrypt should match exact case");
        assertFalse(matchesIncorrect, "BCrypt should not match different case");
    }

    @Test
    @DisplayName("Should handle special characters in passwords")
    void shouldHandleSpecialCharactersInPasswords() {
        // Given
        String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()";

        // When
        String hashedPassword = passwordEncoder.encode(passwordWithSpecialChars);
        boolean matches = passwordEncoder.matches(passwordWithSpecialChars, hashedPassword);

        // Then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$"),
                "Password with special characters should be hashed correctly");
        assertTrue(matches, "PasswordEncoder should match password with special characters");
    }

    @Test
    @DisplayName("Should handle Unicode characters in passwords")
    void shouldHandleUnicodeCharactersInPasswords() {
        // Given
        String passwordWithUnicode = "Pässwörd🔒";

        // When
        String hashedPassword = passwordEncoder.encode(passwordWithUnicode);
        boolean matches = passwordEncoder.matches(passwordWithUnicode, hashedPassword);

        // Then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$"),
                "Password with Unicode characters should be hashed correctly");
        assertTrue(matches, "PasswordEncoder should match password with Unicode characters");
    }

    @Test
    @DisplayName("Should handle long passwords")
    void shouldHandleLongPasswords() {
        // Given
        String longPassword = "A".repeat(100);

        // When
        String hashedPassword = passwordEncoder.encode(longPassword);
        boolean matches = passwordEncoder.matches(longPassword, hashedPassword);

        // Then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$"),
                "Long password should be hashed correctly");
        assertTrue(matches, "PasswordEncoder should match long password");
    }

    @Test
    @DisplayName("Should handle empty password")
    void shouldHandleEmptyPassword() {
        // Given
        String emptyPassword = "";

        // When
        String hashedPassword = passwordEncoder.encode(emptyPassword);
        boolean matches = passwordEncoder.matches(emptyPassword, hashedPassword);

        // Then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$"),
                "Empty password should be hashed correctly");
        assertTrue(matches, "PasswordEncoder should match empty password");
    }

    @Test
    @DisplayName("Should handle passwords with spaces")
    void shouldHandlePasswordsWithSpaces() {
        // Given
        String passwordWithSpaces = "Password With Spaces 123";

        // When
        String hashedPassword = passwordEncoder.encode(passwordWithSpaces);
        boolean matches = passwordEncoder.matches(passwordWithSpaces, hashedPassword);

        // Then
        assertNotNull(hashedPassword);
        assertTrue(matches, "PasswordEncoder should match password with spaces");
    }
}

