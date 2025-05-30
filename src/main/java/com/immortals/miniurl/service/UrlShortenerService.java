package com.immortals.miniurl.service;


import com.immortals.miniurl.model.dto.MiniUrlRequestDto;
import com.immortals.miniurl.model.dto.MiniUrlResponseDto;

public interface UrlShortenerService {

    MiniUrlResponseDto createShortUrl(MiniUrlRequestDto miniUrlRequestDto);

    String getLongUrl(String shortUrl);
}
