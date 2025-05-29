package com.immortals.miniurl.service.exception;

public class UrlShorteningException extends RuntimeException {
    public UrlShorteningException(String message) {
        super(message);
    }

    public UrlShorteningException(String message, Exception e) {
        super(message, e);
    }
}
