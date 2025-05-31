package com.immortals.miniurl.service;

import com.immortals.miniurl.factory.UrlShorteningStrategy;
import com.immortals.miniurl.factory.UrlShorteningStrategyFactory;
import com.immortals.miniurl.model.domain.UrlMapping;
import com.immortals.miniurl.model.dto.CachedUrlDataDto;
import com.immortals.miniurl.model.security.CurrentUserProvider;
import com.immortals.miniurl.repository.UrlMappingRepository;
import com.immortals.miniurl.service.cache.CacheService;
import com.immortals.miniurl.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlShortenerServiceImplTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private UrlShorteningStrategyFactory strategyFactory;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private CacheService<String, String> cacheService;

    @Mock
    private UrlShorteningStrategy mockStrategy;

    @InjectMocks
    private UrlShortenerServiceImpl urlShortenerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(urlShortenerService, "address", "localhost");
        ReflectionTestUtils.setField(urlShortenerService, "port", 8080L);
    }

    @BeforeEach
    void setupSecurityContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken("testUser", null, List.of());
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.getAuthentication())
                .thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testGetLongUrl_fromCache() {
        String shortUrl = "localhost:8080/abc123";
        String originalUrl = "https://example.com";

        CachedUrlDataDto dto = new CachedUrlDataDto(originalUrl, Instant.now()
                .plusSeconds(3600));
        when(cacheService.get(shortUrl)).thenReturn(JsonUtils.toJson(dto));

        String result = urlShortenerService.getLongUrl(shortUrl);

        assertEquals(originalUrl, result);
        verify(cacheService).get(shortUrl);
    }

    @Test
    void testGetLongUrl_fallbackToDB() {
        String shortUrl = "localhost:8080/abc123";
        String originalUrl = "https://example.com";

        when(cacheService.get(shortUrl)).thenReturn(null);
        when(urlMappingRepository.findByShortUrlAndIsActiveTrue(shortUrl))
                .thenReturn(Optional.of(UrlMapping.builder()
                        .originalUrl(originalUrl)
                        .build()));

        String result = urlShortenerService.getLongUrl(shortUrl);
        assertEquals(originalUrl, result);
    }

}
