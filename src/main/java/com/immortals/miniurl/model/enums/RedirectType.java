package com.immortals.miniurl.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RedirectType {
    PERMANENT(301), TEMPORARY(302);

    private final Integer code;

}