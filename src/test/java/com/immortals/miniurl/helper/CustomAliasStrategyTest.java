package com.immortals.miniurl.helper;

import com.immortals.miniurl.service.exception.UrlShorteningException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomAliasStrategyTest {

    private CustomAliasStrategy customAliasStrategy;

    @BeforeEach
    void setUp() {
        List<String> reserved = Arrays.asList("admin", "login", "dashboard");
        customAliasStrategy = new CustomAliasStrategy(reserved);
    }

    @Test
    void testGenerate_withValidAlias_shouldReturnAlias() {
        String originalUrl = "https://example.com";
        String alias = "customAlias123";
        String result = customAliasStrategy.generate(originalUrl, alias);
        assertEquals(alias, result);
    }

    @Test
    void testGenerate_withBlankAlias_shouldThrowException() {
        String originalUrl = "https://example.com";
        UrlShorteningException ex = assertThrows(
                UrlShorteningException.class,
                () -> customAliasStrategy.generate(originalUrl, "")
        );
        assertEquals("Custom alias must be provided and cannot be blank", ex.getMessage());
    }

    @Test
    void testGenerate_withNullAlias_shouldThrowException() {
        String originalUrl = "https://example.com";
        UrlShorteningException ex = assertThrows(
                UrlShorteningException.class,
                () -> customAliasStrategy.generate(originalUrl, (String) null)
        );
        assertEquals("Custom alias must be provided and cannot be blank", ex.getMessage());
    }

    @Test
    void testGenerate_withNoParams_shouldThrowException() {
        String originalUrl = "https://example.com";
        UrlShorteningException ex = assertThrows(
                UrlShorteningException.class,
                () -> customAliasStrategy.generate(originalUrl)
        );
        assertEquals("Custom alias must be provided and cannot be blank", ex.getMessage());
    }

    @Test
    void testGenerate_withInvalidPatternAlias_shouldThrowException() {
        String originalUrl = "https://example.com";
        String invalidAlias = "ab";  // Too short
        UrlShorteningException ex = assertThrows(
                UrlShorteningException.class,
                () -> customAliasStrategy.generate(originalUrl, invalidAlias)
        );
        assertTrue(ex.getMessage()
                .contains("Custom alias is invalid"));
    }

    @Test
    void testGenerate_withReservedAlias_shouldThrowException() {
        String originalUrl = "https://example.com";
        String reservedAlias = "Admin"; // Case-insensitive match

        UrlShorteningException ex = assertThrows(
                UrlShorteningException.class,
                () -> customAliasStrategy.generate(originalUrl, reservedAlias)
        );

        assertEquals("This alias is reserved and cannot be used. Please choose another.", ex.getMessage());
    }
}
