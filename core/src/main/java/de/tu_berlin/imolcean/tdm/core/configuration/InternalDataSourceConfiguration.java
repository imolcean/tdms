package de.tu_berlin.imolcean.tdm.core.configuration;

import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalDataSourceConfiguration
{
    @Value("${app.datasource.internal.driver-class-name}")
    private String driver;

    @Value("${app.datasource.internal.url}")
    private String url;

    @Value("${app.datasource.internal.username}")
    private String username;

    @Value("${app.datasource.internal.password}")
    private String password;

    @Bean(name = "InternalDataSource")
    public DataSourceWrapper internalDataSource()
    {
        return new DataSourceWrapper(driver, url, username, password);
    }
}
