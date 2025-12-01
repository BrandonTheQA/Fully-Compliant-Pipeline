package com.example.ecompoc.order.notification.controller;

import com.example.ecompoc.order.notification.model.NotificationPreferences;
import com.example.ecompoc.order.notification.service.NotificationPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for notification preferences management
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Preferences", description = "Notification preferences management API endpoints")
public class NotificationPreferencesController {
    
    private final NotificationPreferencesService preferencesService;
    
    public NotificationPreferencesController(NotificationPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }
    
    /**
     * GET /api/notifications/preferences?userId={userId} - Get notification preferences
     */
    @Operation(
            summary = "Get notification preferences",
            description = "Retrieves notification preferences for the specified user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences found",
                    content = @Content(schema = @Schema(implementation = NotificationPreferences.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferences> getPreferences(
            @Parameter(description = "User ID", required = true, example = "user123")
            @RequestParam String userId) {
        NotificationPreferences preferences = preferencesService.getPreferences(userId);
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * PUT /api/notifications/preferences?userId={userId} - Update notification preferences
     */
    @Operation(
            summary = "Update notification preferences",
            description = "Updates notification preferences for the specified user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences updated successfully",
                    content = @Content(schema = @Schema(implementation = NotificationPreferences.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferences> updatePreferences(
            @Parameter(description = "User ID", required = true, example = "user123")
            @RequestParam String userId,
            @Parameter(description = "Notification preferences", required = true)
            @RequestBody NotificationPreferences preferences) {
        NotificationPreferences updated = preferencesService.updatePreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }
}
