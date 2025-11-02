package com.example.repository;

import com.example.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * In-memory repository for User entities
 */
@Repository
public class UserRepository {
    
    private final Map<String, User> users = new HashMap<>();
    
    /**
     * Save a user
     */
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }
    
    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }
    
    /**
     * Get total user count
     */
    public long count() {
        return users.size();
    }
}
