package com.immortals.miniurl.controller;

import com.immortals.miniurl.model.dto.MiniUrlRequestDto;
import com.immortals.miniurl.model.dto.MiniUrlResponseDto;
import com.immortals.miniurl.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/url")
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ADMIN')")
    @PostMapping(value = "/shorten", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public MiniUrlResponseDto createShortUrl(@RequestBody @Valid MiniUrlRequestDto miniUrlRequestDto) {
        return urlShortenerService.createShortUrl(miniUrlRequestDto);
    }

    @GetMapping("/redirect/{shortUrl}")
    public void redirectToLongUrl(@PathVariable String shortUrl, HttpServletResponse response) throws IOException {
        String longUrl = urlShortenerService.getLongUrl(shortUrl);
        if (longUrl != null) {
            response.sendRedirect(longUrl);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Short URL not found");
        }
    }

}
