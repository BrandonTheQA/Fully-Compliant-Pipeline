package com.example.monolith.user.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * User domain model
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;
    
    @Column(name = "email", nullable = false, unique = true, columnDefinition = "NVARCHAR(255)")
    private String email;
    
    @Column(name = "password", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String password;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public User() {}
    
    // Constructor with all fields
    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
