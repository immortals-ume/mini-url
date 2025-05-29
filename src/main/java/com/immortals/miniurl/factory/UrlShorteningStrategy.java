package com.immortals.miniurl.factory;

public interface UrlShorteningStrategy {
    String generate(String originalUrl, String... params);
}