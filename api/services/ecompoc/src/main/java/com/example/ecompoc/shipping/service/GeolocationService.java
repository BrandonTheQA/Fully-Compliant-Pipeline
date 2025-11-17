package com.example.ecompoc.shipping.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Service for detecting user geographic location/region
 */
@Service
public class GeolocationService {
    
    private static final String DEFAULT_REGION = "US";
    
    /**
     * Detect user region from HTTP request
     * Uses Accept-Language header or falls back to default region
     * 
     * @return Region code (e.g., "US", "CA")
     */
    public String detectRegion() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Try to get region from Accept-Language header
                String acceptLanguage = request.getHeader("Accept-Language");
                if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
                    // Parse Accept-Language header (e.g., "en-US,en;q=0.9")
                    String region = extractRegionFromAcceptLanguage(acceptLanguage);
                    if (region != null && !region.isEmpty()) {
                        return region.toUpperCase();
                    }
                }
                
                // Try to get region from X-Forwarded-For or other headers
                // For POC, we'll use a simple approach
                // In production, could use IP geolocation service
            }
        } catch (Exception e) {
            // Log error but don't fail - fall back to default
            // For POC, we'll silently fall back
        }
        
        return DEFAULT_REGION;
    }
    
    /**
     * Extract region code from Accept-Language header
     * Examples: "en-US" -> "US", "en-CA" -> "CA", "fr-CA" -> "CA"
     */
    private String extractRegionFromAcceptLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return null;
        }
        
        // Parse the first locale from Accept-Language header
        String[] parts = acceptLanguage.split(",");
        if (parts.length > 0) {
            String firstLocale = parts[0].trim().split(";")[0];
            
            // Check if it contains a region code (e.g., "en-US", "fr-CA")
            if (firstLocale.contains("-")) {
                String[] localeParts = firstLocale.split("-");
                if (localeParts.length >= 2) {
                    String region = localeParts[1];
                    // Map common country codes
                    if (region.length() == 2) {
                        return region;
                    }
                }
            }
            
            // Try to map language to region (simple mapping for POC)
            // en -> US, fr -> CA (simplified)
            if (firstLocale.startsWith("fr")) {
                return "CA";
            } else if (firstLocale.startsWith("en")) {
                return "US";
            }
        }
        
        return null;
    }
    
    /**
     * Detect region with explicit region parameter (for testing/override)
     * 
     * @param regionOverride Optional region override
     * @return Region code
     */
    public String detectRegion(String regionOverride) {
        if (regionOverride != null && !regionOverride.isEmpty()) {
            return regionOverride.toUpperCase();
        }
        return detectRegion();
    }
}

