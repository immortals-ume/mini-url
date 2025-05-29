package com.immortals.miniurl.service;


import com.immortals.miniurl.model.dto.UrlShortenerDto;

public interface UrlShortenerService {

    String createShortUrl(UrlShortenerDto urlShortenerDto);

    String getLongUrl(String shortUrl);

}
