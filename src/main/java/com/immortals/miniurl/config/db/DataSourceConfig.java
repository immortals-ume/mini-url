package com.immortals.miniurl.config.db;

import com.immortals.miniurl.routing.RoutingDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties({
        ReadDataSourceProperties.class,
        WriteDataSourceProperties.class
})
@RequiredArgsConstructor
public class DataSourceConfig {

    private final WriteDataSourceProperties writeDataSourceProperties;
    private final ReadDataSourceProperties readDataSourceProperties;

    @Bean("writeDataSource")
    public DataSource writeDataSource(@Qualifier("writeDataSourceProperties") WriteDataSourceProperties props) {
        return DataSourceBuilder.create()
                .url(props.getUrl())
                .username(props.getUsername())
                .password(props.getPassword())
                .driverClassName(props.getDriverClassName())
                .build();
    }

    @Bean("readDataSource")
    public DataSource readDataSource(@Qualifier("readDataSourceProperties") ReadDataSourceProperties props) {
        return DataSourceBuilder.create()
                .url(props.getUrl())
                .username(props.getUsername())
                .password(props.getPassword())
                .driverClassName(props.getDriverClassName())
                .build();
    }


    @Bean
    public DataSource routingDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("WRITE", writeDataSource(writeDataSourceProperties));
        targetDataSources.put("READ", readDataSource(readDataSourceProperties));

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(writeDataSource(writeDataSourceProperties));
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }
}
