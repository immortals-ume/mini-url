package com.immortals.miniurl.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CachedUrlData {
    private String originalUrl;
    private Instant expiresAt;
}