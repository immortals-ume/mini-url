package com.immortals.miniurl.config.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "swagger")
@Getter
@Setter
public class SwaggerProperties {
    private String title;
    private String description;
    private String version;
    private String termsOfService;
    private String contact;
    private License license;
    private List<Server> servers;
    private Boolean enabled;

    @Getter
    @Setter
    public static class License {
        private String name;
        private String url;
    }

    @Getter
    @Setter
    public static class Server {
        private String url;
        private String description;
    }
}
