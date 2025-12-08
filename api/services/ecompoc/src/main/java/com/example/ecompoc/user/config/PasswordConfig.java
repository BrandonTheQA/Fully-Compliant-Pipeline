package com.example.ecompoc.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for password hashing using BCrypt
 */
@Configuration
public class PasswordConfig {

    @Value("${password.hashing.bcrypt-strength:10}")
    private int bcryptStrength;

    /**
     * Creates a BCryptPasswordEncoder bean with configurable strength
     * 
     * Strength parameter (cost factor) determines the computational cost:
     * - 10 is the default and recommended for most applications
     * - Higher values (11-12) provide more security but slower hashing
     * - Lower values (< 10) are not recommended for production
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Ensure strength is at least 10 for security
        int strength = Math.max(bcryptStrength, 10);
        return new BCryptPasswordEncoder(strength);
    }
}

