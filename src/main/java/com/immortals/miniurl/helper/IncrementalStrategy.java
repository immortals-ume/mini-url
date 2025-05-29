package com.immortals.miniurl.helper;

import com.immortals.miniurl.factory.UrlShorteningStrategy;
import com.immortals.miniurl.utils.Base62Utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * IncrementalStrategy generates a unique incremental ID encoded in Base62.
 * Thread-safe and configurable starting point.
 */
public final class IncrementalStrategy implements UrlShorteningStrategy {

    private final AtomicLong counter;

    /**
     * Default constructor starting counter at 1.
     */
    public IncrementalStrategy() {
        this(1L);
    }

    /**
     * Constructor with configurable initial counter value.
     *
     * @param initialValue initial counter value; must be >= 0
     */
    public IncrementalStrategy(long initialValue) {
        if (initialValue < 0) {
            throw new IllegalArgumentException("Initial counter value must be non-negative");
        }
        this.counter = new AtomicLong(initialValue);
    }

    @Override
    public String generate(String originalUrl, String... params) {
        long id = counter.getAndIncrement();
        // Optionally handle overflow, e.g., reset counter or throw exception
        return Base62Utils.encode(id);
    }
}
