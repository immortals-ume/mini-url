package com.immortals.miniurl.factory;

import com.immortals.miniurl.context.StrategyContext;
import com.immortals.miniurl.model.enums.UrlStrategyType;
import org.springframework.stereotype.Component;

@Component
public class SmartUrlStrategySelectorFactory {

    public static UrlStrategyType selectStrategy(StrategyContext context) {
        if (context.isCustomAlias()) return UrlStrategyType.CUSTOM_ALIAS;

        if (context.isPremiumUser()) {
            return context.isHighThroughput() ? UrlStrategyType.SNOWFLAKE : UrlStrategyType.HASH;
        }

        if (context.isUseTimestamp()) {
            return UrlStrategyType.TIMESTAMP_RANDOM;
        }

        if (context.needsDeterminism()) {
            return UrlStrategyType.HASH;
        }

        if (context.isInternalTool()) {
            return UrlStrategyType.INCREMENTAL;
        }

        return UrlStrategyType.RANDOM;
    }
}
