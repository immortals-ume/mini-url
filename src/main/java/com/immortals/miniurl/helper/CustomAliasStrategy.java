package com.immortals.miniurl.helper;

import com.immortals.miniurl.factory.UrlShorteningStrategy;
import com.immortals.miniurl.service.exception.UrlShorteningException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class CustomAliasStrategy implements UrlShorteningStrategy {

    private static final String ALIAS_PATTERN = "^[a-zA-Z0-9-_]{3,30}$";

    private final Set<String> reservedAliases;

    public CustomAliasStrategy(List<String> reservedAliases) {
        this.reservedAliases = new HashSet<>();
        for (String alias : reservedAliases) {
            this.reservedAliases.add(alias.toLowerCase());
        }
    }

    @Override
    public String generate(String originalUrl, String... params) {
        if (params.length == 0 || params[0] == null || params[0].isBlank()) {
            log.warn("Custom alias not provided for URL: {}", originalUrl);
            throw new UrlShorteningException("Custom alias must be provided and cannot be blank");
        }

        String alias = params[0].trim();

        if (!alias.matches(ALIAS_PATTERN)) {
            log.warn("Invalid custom alias '{}' for URL: {}", alias, originalUrl);
            throw new UrlShorteningException(
                    "Custom alias is invalid. Allowed characters: a-z, A-Z, 0-9, dash (-), underscore (_). Length must be 3 to 30 characters.");
        }

        if (reservedAliases.contains(alias.toLowerCase())) {
            log.warn("Reserved alias '{}' attempted for URL: {}", alias, originalUrl);
            throw new UrlShorteningException("This alias is reserved and cannot be used. Please choose another.");
        }

        return alias;
    }
}
