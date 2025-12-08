package com.example.ecompoc.user.migration;

import com.example.ecompoc.user.model.User;
import com.example.ecompoc.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to migrate existing plaintext passwords to BCrypt hashes on application startup.
 * 
 * This migration is idempotent - it only processes passwords that are not already hashed.
 * BCrypt hashes start with $2a$, $2b$, or $2y$ followed by the strength parameter.
 */
@Component
public class PasswordMigrationService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PasswordMigrationService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private boolean migrationExecuted = false;

    @Autowired
    public PasswordMigrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executes password migration on application startup.
     * Only runs once per application context lifecycle.
     */
    @Override
    @Transactional
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        // Prevent multiple executions in the same application context
        if (migrationExecuted) {
            return;
        }
        
        try {
            migratePasswords();
            migrationExecuted = true;
        } catch (Exception e) {
            logger.error("Password migration failed", e);
            // Don't throw exception to allow application to start
            // Migration can be retried on next startup
        }
    }

    /**
     * Migrates all plaintext passwords to BCrypt hashes.
     * Only processes passwords that are not already hashed.
     */
    @Transactional
    public void migratePasswords() {
        List<User> users = userRepository.findAll();
        int migratedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        logger.info("Starting password migration for {} users", users.size());

        for (User user : users) {
            try {
                String password = user.getPassword();
                
                // Skip if password is null or empty
                if (password == null || password.trim().isEmpty()) {
                    logger.warn("Skipping user {} - password is null or empty", user.getEmail());
                    skippedCount++;
                    continue;
                }

                // Check if password is already hashed (BCrypt hashes start with $2a$, $2b$, or $2y$)
                if (isPasswordHashed(password)) {
                    logger.debug("Skipping user {} - password already hashed", user.getEmail());
                    skippedCount++;
                    continue;
                }

                // Hash the plaintext password
                String hashedPassword = passwordEncoder.encode(password);
                user.setPassword(hashedPassword);
                userRepository.save(user);
                
                migratedCount++;
                logger.debug("Migrated password for user {}", user.getEmail());
                
            } catch (Exception e) {
                errorCount++;
                logger.error("Failed to migrate password for user {}: {}", user.getEmail(), e.getMessage());
            }
        }

        logger.info("Password migration completed - Migrated: {}, Skipped: {}, Errors: {}", 
                    migratedCount, skippedCount, errorCount);
    }

    /**
     * Checks if a password is already hashed using BCrypt.
     * BCrypt hashes start with $2a$, $2b$, or $2y$ followed by strength parameter.
     * 
     * @param password The password to check
     * @return true if password appears to be a BCrypt hash, false otherwise
     */
    private boolean isPasswordHashed(String password) {
        if (password == null || password.length() < 7) {
            return false;
        }
        
        // BCrypt hashes start with $2a$, $2b$, or $2y$ followed by strength (usually 10-12)
        // Format: $2a$10$... (60 characters total)
        return password.startsWith("$2a$") || 
               password.startsWith("$2b$") || 
               password.startsWith("$2y$");
    }
}

