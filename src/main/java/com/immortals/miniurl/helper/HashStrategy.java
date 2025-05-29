package com.immortals.miniurl.helper;

import com.immortals.miniurl.factory.UrlShorteningStrategy;
import com.immortals.miniurl.service.exception.UrlShorteningException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.immortals.miniurl.constants.UrlConstants.MAX_SHA256_BASE64URL_LENGTH;

/**
 * URL shortening strategy that generates a fixed-length short URL
 * based on a hash digest of the original URL.
 */
@Slf4j
public class HashStrategy implements UrlShorteningStrategy {

    private final int length;
    private final String algorithm;

    /**
     * @param length    the desired length of the short URL must be > 0 and <= max length for algorithm output
     * @param algorithm hashing algorithm name like "SHA-256"
     */
    public HashStrategy(int length, String algorithm) {
        if (length <= 0) {
            throw new UrlShorteningException("Length must be positive");
        }
        if ("SHA-256".equalsIgnoreCase(algorithm) && length > MAX_SHA256_BASE64URL_LENGTH) {
            throw new UrlShorteningException("Length cannot exceed " + MAX_SHA256_BASE64URL_LENGTH + " for SHA-256");
        }
        this.length = length;
        this.algorithm = algorithm;
    }

    @Override
    public String generate(String originalUrl, String... params) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            throw new UrlShorteningException("Original URL must not be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

            if (length > encoded.length()) {
                log.warn("Requested length {} exceeds hash length {}, truncating to max length", length, encoded.length());
                return encoded;
            }

            return encoded.substring(0, length);

        } catch (NoSuchAlgorithmException e) {
            log.error("Hashing algorithm {} not found", algorithm, e);
            throw new UrlShorteningException("Internal error during URL hashing");
        } catch (RuntimeException e) {
            log.error("Unexpected error during URL hashing", e);
            throw new UrlShorteningException("Internal error during URL hashing");
        }
    }
}
