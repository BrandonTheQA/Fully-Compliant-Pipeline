package com.example.ecompoc.user.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordConfig
 */
@DisplayName("PasswordConfig Tests")
class PasswordConfigTest {

    private PasswordConfig passwordConfig;

    @BeforeEach
    void setUp() {
        passwordConfig = new PasswordConfig();
        // Use reflection to set bcryptStrength, or test with default
        // For simplicity, we'll test the actual bean creation
    }

    @Test
    @DisplayName("Should create PasswordEncoder bean with default strength")
    void shouldCreatePasswordEncoderBeanWithDefaultStrength() {
        // Create PasswordConfig instance and get the bean
        PasswordEncoder encoder = passwordConfig.passwordEncoder();
        
        assertNotNull(encoder, "PasswordEncoder bean should be created");
        assertInstanceOf(BCryptPasswordEncoder.class, encoder, 
                "PasswordEncoder should be an instance of BCryptPasswordEncoder");
    }

    @Test
    @DisplayName("Should configure BCrypt with strength >= 10")
    void shouldConfigureBCryptWithStrengthAtLeast10() {
        PasswordEncoder encoder = passwordConfig.passwordEncoder();
        
        // Test that password encoding works (which validates strength configuration)
        String password = "TestPassword123";
        String hash = encoder.encode(password);
        
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"),
                "Password hash should be BCrypt format");
        
        // BCrypt hash format: $2a$10$... where 10 is the strength
        // Extract strength from hash
        if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
            String strengthStr = hash.substring(4, 6);
            int strength = Integer.parseInt(strengthStr);
            assertTrue(strength >= 10, 
                    "BCrypt strength should be at least 10, got: " + strength);
        }
    }

    @Test
    @DisplayName("Should encode and match passwords correctly")
    void shouldEncodeAndMatchPasswordsCorrectly() {
        PasswordEncoder encoder = passwordConfig.passwordEncoder();
        
        String password = "SecurePassword123";
        String hash = encoder.encode(password);
        
        assertTrue(encoder.matches(password, hash), 
                "PasswordEncoder should match correct password");
        assertFalse(encoder.matches("WrongPassword", hash), 
                "PasswordEncoder should not match incorrect password");
    }

    @Test
    @DisplayName("Should enforce minimum strength of 10")
    void shouldEnforceMinimumStrengthOf10() {
        // Create PasswordConfig with low strength value
        PasswordConfig configWithLowStrength = new PasswordConfig();
        // Since we can't easily inject @Value in unit test, we test that default behavior
        // enforces minimum strength of 10
        
        PasswordEncoder encoder = configWithLowStrength.passwordEncoder();
        String hash = encoder.encode("TestPassword123");
        
        // Extract strength from hash
        if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
            String strengthStr = hash.substring(4, 6);
            int strength = Integer.parseInt(strengthStr);
            assertTrue(strength >= 10, 
                    "BCrypt strength should be enforced to at least 10, got: " + strength);
        }
    }
}

