package com.example.ecompoc.order.notification.repository;

import com.example.ecompoc.order.notification.model.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for notification preferences
 */
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, String> {
    
    /**
     * Find notification preferences by user ID
     */
    Optional<NotificationPreferences> findByUserId(String userId);
}
