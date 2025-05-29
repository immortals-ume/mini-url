package com.immortals.miniurl.config.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "cache.redis")
public class CacheProperties {

    private String host;
    private Integer port;
    private String password;
    private Integer database;
    private Duration commandTimeout;
    private Boolean useSsl;
    private Boolean autoReconnect;
    private Integer poolMaxTotal;
    private Integer poolMaxIdle;
    private Integer poolMinIdle;
    private Duration poolMaxWait;
    private Boolean enabled;
    private Boolean pingBeforeActivateConnection;

    private Cluster cluster = new Cluster();
    private Sentinel sentinel = new Sentinel();

    @Getter
    @Setter
    public static class Cluster {
        private List<String> nodes = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Sentinel {
        private String master;
        private List<String> nodes = new ArrayList<>();
    }
}
