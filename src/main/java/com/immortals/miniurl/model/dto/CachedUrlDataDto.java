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
public class CachedUrlDataDto {
    private String originalUrl;
    private Instant expiresAt;
}