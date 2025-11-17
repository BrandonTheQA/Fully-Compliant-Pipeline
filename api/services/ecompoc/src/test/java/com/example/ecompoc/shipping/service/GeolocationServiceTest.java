package com.example.ecompoc.shipping.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GeolocationService
 */
@DisplayName("GeolocationService Tests")
class GeolocationServiceTest {

    private GeolocationService geolocationService;

    @BeforeEach
    void setUp() {
        geolocationService = new GeolocationService();
    }

    @Test
    @DisplayName("Should return default region when no request context")
    void shouldReturnDefaultRegionWhenNoRequestContext() {
        // Clear any existing request context
        RequestContextHolder.resetRequestAttributes();

        // When
        String region = geolocationService.detectRegion();

        // Then
        assertNotNull(region);
        assertEquals("US", region);
    }

    @Test
    @DisplayName("Should detect region from Accept-Language header with US locale")
    void shouldDetectRegionFromAcceptLanguageHeaderWithUSLocale() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en-US,en;q=0.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            // When
            String region = geolocationService.detectRegion();

            // Then
            assertNotNull(region);
            assertEquals("US", region);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Should detect region from Accept-Language header with CA locale")
    void shouldDetectRegionFromAcceptLanguageHeaderWithCALocale() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en-CA,en;q=0.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            // When
            String region = geolocationService.detectRegion();

            // Then
            assertNotNull(region);
            assertEquals("CA", region);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Should detect region from French locale as CA")
    void shouldDetectRegionFromFrenchLocaleAsCA() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "fr-CA,fr;q=0.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            // When
            String region = geolocationService.detectRegion();

            // Then
            assertNotNull(region);
            assertEquals("CA", region);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Should fallback to default when Accept-Language header is missing")
    void shouldFallbackToDefaultWhenAcceptLanguageHeaderIsMissing() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            // When
            String region = geolocationService.detectRegion();

            // Then
            assertNotNull(region);
            assertEquals("US", region);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Should use region override when provided")
    void shouldUseRegionOverrideWhenProvided() {
        // When
        String region = geolocationService.detectRegion("CA");

        // Then
        assertNotNull(region);
        assertEquals("CA", region);
    }

    @Test
    @DisplayName("Should use region override with case-insensitive input")
    void shouldUseRegionOverrideWithCaseInsensitiveInput() {
        // When
        String region1 = geolocationService.detectRegion("ca");
        String region2 = geolocationService.detectRegion("Ca");
        String region3 = geolocationService.detectRegion("cA");

        // Then
        assertEquals("CA", region1);
        assertEquals("CA", region2);
        assertEquals("CA", region3);
    }

    @Test
    @DisplayName("Should fallback to auto-detect when region override is null")
    void shouldFallbackToAutoDetectWhenRegionOverrideIsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en-US");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            // When
            String region = geolocationService.detectRegion(null);

            // Then
            assertNotNull(region);
            assertEquals("US", region);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Should fallback to auto-detect when region override is empty")
    void shouldFallbackToAutoDetectWhenRegionOverrideIsEmpty() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en-US");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            // When
            String region = geolocationService.detectRegion("");

            // Then
            assertNotNull(region);
            assertEquals("US", region);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Should handle malformed Accept-Language header gracefully")
    void shouldHandleMalformedAcceptLanguageHeaderGracefully() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "invalid-format");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            // When
            String region = geolocationService.detectRegion();

            // Then
            assertNotNull(region);
            // Should fallback to default or try to parse
            assertTrue(region.equals("US") || region.equals("CA"));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}

