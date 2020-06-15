package de.tu_berlin.imolcean.tdm.configuration;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ExternalDataSourceConfiguration
{
    @Value("${app.datasource.dev.server}")
    private String serverName;

    @Value("${app.datasource.dev.port}")
    private int portNumber;

    @Value("${app.datasource.dev.db}")
    private String databaseName;

    @Value("${app.datasource.dev.user}")
    private String username;

    @Value("${app.datasource.dev.password}")
    private String password;

    @Bean(name = "ExternalDataSource")
    public DataSource internalDataSource()
    {
        SQLServerDataSource ds = new SQLServerDataSource();

        ds.setServerName(this.serverName);
        ds.setPortNumber(this.portNumber);
        ds.setDatabaseName(this.databaseName);
        ds.setUser(this.username);
        ds.setPassword(this.password);

        return ds;
    }
}
