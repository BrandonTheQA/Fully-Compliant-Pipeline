package com.example.repository;

import com.example.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Repository
public class UserRepository {

    private final Map<String, User> users = new HashMap<>();

    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(String id) { return Optional.ofNullable(users.get(id)); }

    public Optional<User> findByEmail(String email) {
        return users.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    public long count() { return users.size(); }
}
