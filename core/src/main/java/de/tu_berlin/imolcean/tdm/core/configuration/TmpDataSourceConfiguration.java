package de.tu_berlin.imolcean.tdm.core.configuration;

import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TmpDataSourceConfiguration
{
    @Value("${app.datasource.tmp.driver-class-name}")
    private String driver;

    @Value("${app.datasource.tmp.url}")
    private String url;

    @Value("${app.datasource.tmp.username}")
    private String username;

    @Value("${app.datasource.tmp.password}")
    private String password;

    @Bean(name = "TmpDataSource")
    public DataSourceWrapper tmpDataSource()
    {
        return new DataSourceWrapper(driver, url, username, password);
    }
}
