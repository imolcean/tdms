package de.tu_berlin.imolcean.tdm.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class InternalDataSourceConfiguration
{
    @Value("${app.datasource.ru2.driver-class-name}")
    private String driver;

    @Value("${app.datasource.ru2.url}")
    private String url;

    @Value("${app.datasource.ru2.user}")
    private String username;

    @Value("${app.datasource.ru2.password}")
    private String password;

    @Bean(name = "InternalDataSource")
    public DataSource externalRu2DataSource()
    {
        return DataSourceBuilder.create()
                .driverClassName(this.driver)
                .url(this.url)
                .username(this.username)
                .password(this.password)
                .build();
    }
}
