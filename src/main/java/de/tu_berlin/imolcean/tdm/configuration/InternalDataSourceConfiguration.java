package de.tu_berlin.imolcean.tdm.configuration;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalDataSourceConfiguration
{
    @Value("${app.datasource.internal.server}")
    private String serverName;

    @Value("${app.datasource.internal.port}")
    private int portNumber;

    @Value("${app.datasource.internal.db}")
    private String databaseName;

    @Value("${app.datasource.internal.user}")
    private String username;

    @Bean(name = "InternalDataSource")
    public SQLServerDataSource internalDataSource()
    {
        SQLServerDataSource ds = new SQLServerDataSource();

        ds.setServerName(this.serverName);
        ds.setPortNumber(this.portNumber);
        ds.setDatabaseName(this.databaseName);
        ds.setUser(this.username);
        ds.setIntegratedSecurity(true);

        return ds;
    }
}
