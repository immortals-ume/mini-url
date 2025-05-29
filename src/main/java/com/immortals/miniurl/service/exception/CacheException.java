package com.immortals.miniurl.service.exception;


public class CacheException extends RuntimeException {
    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, RuntimeException e) {
        super(message, e);
    }
}
