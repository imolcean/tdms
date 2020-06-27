package de.tu_berlin.imolcean.tdm.core.configuration;

import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
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
    public DataSourceProxy externalRu2DataSource()
    {
        return new DataSourceProxy(driver, url, username, password);
    }
}
