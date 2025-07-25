package com.immortals.miniurl.config.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "datasource.write")
public class WriteDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
