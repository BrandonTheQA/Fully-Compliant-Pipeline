package com.example.ecompoc.order.notification.service;

import com.example.ecompoc.order.notification.model.NotificationPreferences;
import com.example.ecompoc.order.notification.repository.NotificationPreferencesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing notification preferences
 */
@Service
public class NotificationPreferencesService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferencesService.class);
    
    private final NotificationPreferencesRepository preferencesRepository;
    
    public NotificationPreferencesService(NotificationPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }
    
    /**
     * Get notification preferences for a user
     */
    public NotificationPreferences getPreferences(String userId) {
        return preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create default preferences if not found
                    NotificationPreferences defaultPrefs = new NotificationPreferences();
                    defaultPrefs.setId(UUID.randomUUID().toString());
                    defaultPrefs.setUserId(userId);
                    defaultPrefs.setEmailEnabled(true);
                    defaultPrefs.setSmsEnabled(false);
                    defaultPrefs.setNotificationFrequency("ALL");
                    defaultPrefs.setCreatedAt(LocalDateTime.now());
                    defaultPrefs.setUpdatedAt(LocalDateTime.now());
                    return preferencesRepository.save(defaultPrefs);
                });
    }
    
    /**
     * Update notification preferences for a user
     */
    @Transactional
    public NotificationPreferences updatePreferences(String userId, NotificationPreferences preferences) {
        NotificationPreferences existing = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreferences newPrefs = new NotificationPreferences();
                    newPrefs.setId(UUID.randomUUID().toString());
                    newPrefs.setUserId(userId);
                    newPrefs.setCreatedAt(LocalDateTime.now());
                    return newPrefs;
                });
        
        existing.setEmailEnabled(preferences.getEmailEnabled());
        existing.setSmsEnabled(preferences.getSmsEnabled());
        existing.setPhoneNumber(preferences.getPhoneNumber());
        existing.setNotificationFrequency(preferences.getNotificationFrequency());
        existing.setUpdatedAt(LocalDateTime.now());
        
        NotificationPreferences saved = preferencesRepository.save(existing);
        logger.info("Updated notification preferences for user {}", userId);
        return saved;
    }
    
    /**
     * Get preferences for an order's user
     */
    public NotificationPreferences getPreferencesForOrder(String orderId, String userId) {
        return getPreferences(userId);
    }
}
