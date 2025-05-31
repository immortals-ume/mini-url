package com.immortals.miniurl.controller;

import com.immortals.miniurl.model.dto.MiniUrlRequestDto;
import com.immortals.miniurl.model.dto.MiniUrlResponseDto;
import com.immortals.miniurl.service.UrlShortenerService;
import com.immortals.miniurl.utils.MockValueUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.test.context.support.WithMockUser;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class UrlShortenerControllerTest {

    @InjectMocks
    private UrlShortenerController urlShortenerController;

    @Mock
    private UrlShortenerService urlShortenerService;

    @Mock
    private HttpServletResponse httpServletResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testCreateShortUrl_success() {
        var url = MockValueUtils.generateRandomString();
        MiniUrlRequestDto requestDto = new MiniUrlRequestDto();
        requestDto.setOriginalUrl(url);
        MiniUrlResponseDto expectedResponse = new MiniUrlResponseDto(MockValueUtils.generateRandomString());


        when(urlShortenerService.createShortUrl(requestDto)).thenReturn(expectedResponse);

        MiniUrlResponseDto actualResponse = urlShortenerController.createShortUrl(requestDto);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getShortUrl(), actualResponse.getShortUrl());
        verify(urlShortenerService, times(1)).createShortUrl(requestDto);
    }

    @Test
    void testRedirectToLongUrl_success() throws IOException {
        String shortUrl = "abc123";
        String longUrl = "https://example.com";

        when(urlShortenerService.getLongUrl(shortUrl)).thenReturn(longUrl);

        urlShortenerController.redirectToLongUrl(shortUrl, httpServletResponse);

        verify(httpServletResponse, times(1)).sendRedirect(longUrl);
        verify(httpServletResponse, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testRedirectToLongUrl_notFound() throws IOException {
        String shortUrl = "invalid";

        when(urlShortenerService.getLongUrl(shortUrl)).thenReturn(null);

        urlShortenerController.redirectToLongUrl(shortUrl, httpServletResponse);

        verify(httpServletResponse, never()).sendRedirect(anyString());
        verify(httpServletResponse, times(1))
                .sendError(HttpServletResponse.SC_NOT_FOUND, "Short URL not found");
    }
}
