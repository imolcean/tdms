package de.tu_berlin.imolcean.tdm.core.configuration;

import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportDataSourceConfiguration
{
    @Value("${app.datasource.import.driver-class-name}")
    private String driver;

    @Value("${app.datasource.import.url}")
    private String url;

    @Value("${app.datasource.import.username}")
    private String username;

    @Value("${app.datasource.import.password}")
    private String password;

    @Bean(name = "ImportDataSource")
    public DataSourceWrapper internalDataSource()
    {
        return new DataSourceWrapper(driver, url, username, password);
    }
}
