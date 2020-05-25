package de.tu_berlin.imolcean.tdm.configuration;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalRu2DataSourceConfiguration
{
    @Value("${app.datasource.external.ru2.server}")
    private String serverName;

    @Value("${app.datasource.external.ru2.port}")
    private int portNumber;

    @Value("${app.datasource.external.ru2.db}")
    private String databaseName;

    @Value("${app.datasource.external.ru2.user}")
    private String username;

    @Value("${app.datasource.external.ru2.password}")
    private String password;

    @Bean(name = "ExternalRu2DataSource")
    public SQLServerDataSource externalRu2DataSource()
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
