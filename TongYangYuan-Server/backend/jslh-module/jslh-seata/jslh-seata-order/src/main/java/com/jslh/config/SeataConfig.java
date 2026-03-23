package com.jslh.config;

import org.apache.seata.rm.datasource.DataSourceProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class SeataConfig {

    @Primary
    @Bean
    public DataSourceProxy dataSource(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }

}
