package com.immortals.miniurl.controller;

import com.immortals.miniurl.model.dto.UrlShortenerDto;
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
    public String createShortUrl(@RequestBody @Valid UrlShortenerDto urlShortenerDto) {
        return urlShortenerService.createShortUrl(urlShortenerDto);
    }

    @GetMapping("/redirect")
    public void redirectToLongUrl(@RequestParam(value = "shortUrl") String shortUrl, HttpServletResponse response) throws IOException {
        String longUrl = urlShortenerService.getLongUrl(shortUrl);
        response.sendRedirect(longUrl);
    }

}
