package com.immortals.miniurl.factory;

import com.immortals.miniurl.model.enums.UrlStrategyType;
import com.immortals.miniurl.helper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.immortals.miniurl.constants.UrlConstants.*;

@Component
@RequiredArgsConstructor
public class UrlShorteningStrategyFactory {

    public UrlShorteningStrategy getStrategy(UrlStrategyType type) {
        return switch (type) {
            case INCREMENTAL -> new IncrementalStrategy();
            case RANDOM -> new RandomStrategy(RANDOM_LENGTH);
            case HASH -> new HashStrategy(HASH_LENGTH, HASH_ALGORITHM);
            case TIMESTAMP_RANDOM -> new TimestampRandomStrategy(TIMESTAMP_RANDOM_LENGTH);
            case CUSTOM_ALIAS -> new CustomAliasStrategy(List.of(RESERVED_ALIASES));
            case SNOWFLAKE -> new SnowflakeStrategy();
        };
    }
}