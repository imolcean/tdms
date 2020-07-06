package de.tu_berlin.imolcean.tdm.core.configuration;

import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public DataSourceProxy internalDataSource()
    {
        return new DataSourceProxy(driver, url, username, password);
    }
}
