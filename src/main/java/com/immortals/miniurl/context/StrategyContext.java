package com.immortals.miniurl.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StrategyContext {
    private Boolean customAlias;
    private Boolean premiumUser;
    private Boolean highThroughput;
    private Boolean needsDeterminism;
    private Boolean internalTool;

    public Boolean isCustomAlias() {
        return customAlias;
    }

    public Boolean isPremiumUser() {
        return premiumUser;
    }

    public Boolean isHighThroughput() {
        return highThroughput;
    }

    public Boolean needsDeterminism() {
        return needsDeterminism;
    }

    public Boolean isInternalTool() {
        return internalTool;
    }
}
