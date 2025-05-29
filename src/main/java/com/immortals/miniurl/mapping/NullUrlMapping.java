package com.immortals.miniurl.mapping;

import com.immortals.miniurl.model.UrlMapping;
import org.apache.commons.lang3.StringUtils;

public class NullUrlMapping extends UrlMapping {

    @Override
    public Long getUrlMappingId() {
        return 0L;
    }

    @Override
    public String getOriginalUrl() {
        return StringUtils.EMPTY;
    }
}
