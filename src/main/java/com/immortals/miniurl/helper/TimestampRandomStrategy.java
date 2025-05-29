package com.immortals.miniurl.helper;

import com.immortals.miniurl.factory.UrlShorteningStrategy;
import com.immortals.miniurl.service.exception.UrlShorteningException;
import com.immortals.miniurl.utils.Base62Utils;

import java.security.SecureRandom;
import java.util.Objects;

import static com.immortals.miniurl.constants.UrlConstants.ALPHABET;
import static com.immortals.miniurl.constants.UrlConstants.MAX_RANDOM_LENGTH;

public class TimestampRandomStrategy implements UrlShorteningStrategy {


    private static final SecureRandom RANDOM = new SecureRandom();

    private final int randomLength;


    public TimestampRandomStrategy(int randomLength) {
        if (randomLength <= 0 || randomLength > MAX_RANDOM_LENGTH) {
            throw new IllegalArgumentException("randomLength must be between 1 and " + MAX_RANDOM_LENGTH);
        }
        this.randomLength = randomLength;
    }

    @Override
    public String generate(String originalUrl, String... params) {
        try {
            Objects.requireNonNull(originalUrl, "originalUrl cannot be null");

            long timestamp = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder(Long.toString(timestamp));

            for (int i = 0; i < randomLength; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }

            // To encode with Base62Utils, we must convert to a number.
            // Since the appended random part contains letters, parseLong will fail.
            // So instead, encode the timestamp and random string separately, then combine or encode differently.

            // Strategy:
            // 1) Encode timestamp as Base62 string
            // 2) Append random characters directly (they're from the base62 alphabet already)

            String base62Timestamp = Base62Utils.encode(timestamp);
            StringBuilder shortUrl = new StringBuilder(base62Timestamp);

            for (int i = 0; i < randomLength; i++) {
                shortUrl.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }

            return shortUrl.toString();

        } catch (Exception e) {
            throw new UrlShorteningException("Failed to generate short URL: " + e.getMessage(), e);
        }
    }
}
