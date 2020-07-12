package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.pf4j.Extension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

// TODO Configure changelog path
// TODO Pack Liquibase.Core in the plugin jar

@Extension
@Log
@NoArgsConstructor
public class LiquibaseUpdater implements SchemaUpdater
{
    private String changelogPath;

    public LiquibaseUpdater(Properties properties)
    {
        this.changelogPath = properties.getProperty("changelog.path");

        log.fine("Liquibase changelog: " + this.changelogPath);
    }

    @Override
    public void updateSchema(DataSource ds) throws Exception
    {
        log.fine("Updating schema");

        if(changelogPath == null)
        {
            throw new IllegalStateException("Changelog path is not configured");
        }

        try(Connection connection = ds.getConnection())
        {
            Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(new DatabaseChangeLog(changelogPath), new FileSystemResourceAccessor(), db);

            liquibase.update(new Contexts(), new LabelExpression());
        }

        log.fine("Update finished");
    }
}
