package com.immortals.miniurl.helper;

import com.immortals.miniurl.factory.UrlShorteningStrategy;

import java.util.concurrent.ThreadLocalRandom;

import static com.immortals.miniurl.constants.UrlConstants.ALPHABET;

/**
 * RandomStrategy generates a random short URL string of a given length
 * using a configurable alphabet of characters.
 */
public final class RandomStrategy implements UrlShorteningStrategy {

    private final String alphabet;
    private final int length;

    /**
     * Creates a RandomStrategy with specified length and default alphabet.
     *
     * @param length length of the generated string; must be > 0
     */
    public RandomStrategy(int length) {
        this(length, ALPHABET);
    }

    /**
     * Creates a RandomStrategy with specified length and alphabet.
     *
     * @param length   length of the generated string; must be > 0
     * @param alphabet allowed characters for generated string; must not be empty
     */
    public RandomStrategy(int length, String alphabet) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("Alphabet cannot be null or empty");
        }
        this.length = length;
        this.alphabet = alphabet;
    }

    @Override
    public String generate(String originalUrl, String... params) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
