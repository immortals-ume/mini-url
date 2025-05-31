package com.immortals.miniurl.service;

import com.immortals.miniurl.model.domain.UrlMapping;
import com.immortals.miniurl.model.enums.UserTypes;
import com.immortals.miniurl.repository.UrlMappingRepository;
import com.immortals.miniurl.utils.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiryCheckerServiceTest {

    private final Instant fixedNowInstant = Instant.parse("2025-05-31T00:00:00Z");
    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 5, 31, 0, 0);
    @Mock
    private UrlMappingRepository urlMappingRepository;
    @InjectMocks
    private ExpiryCheckerService expiryCheckerService;

    private static void mockStatic(Class<?> clazz) {
        try {
            var method = clazz.getDeclaredMethod("nowInstant");
            method.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Static mocking failed", e);
        }
    }

    @BeforeEach
    void setUp() {
        mockStatic(DateTimeUtils.class);
    }

    @Test
    void shouldMarkExpiredItemsInactiveAndSave() {
        UrlMapping expired = new UrlMapping();
        expired.setIsActive(true);
        expired.setExpiresAt(fixedNowInstant.minusSeconds(3600));

        when(urlMappingRepository.findByExpiresAtBeforeAndIsActiveTrue(any()))
                .thenReturn(List.of(expired));

        expiryCheckerService.runScheduledExpiryJob();

        assert !expired.getIsActive();
        assert expired.getUpdatedBy()
                .equals(UserTypes.SYSTEM.name());

        verify(urlMappingRepository).saveAllAndFlush(List.of(expired));
    }

    @Test
    void shouldDoNothingWhenNoExpiredItems() {
        when(urlMappingRepository.findByExpiresAtBeforeAndIsActiveTrue(any()))
                .thenReturn(Collections.emptyList());

        expiryCheckerService.runScheduledExpiryJob();

        verify(urlMappingRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void shouldHandleExceptionGracefully() {
        expiryCheckerService.runScheduledExpiryJob(); // Should log error but not throw
    }
}
