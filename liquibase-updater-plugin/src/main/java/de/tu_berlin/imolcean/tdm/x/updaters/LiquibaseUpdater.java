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
import lombok.extern.java.Log;
import org.pf4j.Extension;

import javax.sql.DataSource;
import java.sql.Connection;

// TODO Configure changelog path
// TODO Pack Liquibase.Core in the plugin jar

@Extension
@Log
public class LiquibaseUpdater implements SchemaUpdater
{
    @Override
    public void updateSchema(DataSource ds) throws Exception
    {
        log.fine("Updating schema");

        try(Connection connection = ds.getConnection())
        {
            Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            // TODO Path
            // TODO Accessor
            Liquibase liquibase = new Liquibase(getChangeLog(), new FileSystemResourceAccessor(), db);

            liquibase.update(new Contexts(), new LabelExpression());
        }

        log.fine("Update finished");
    }

    private DatabaseChangeLog getChangeLog()
    {
        return new DatabaseChangeLog("C:\\tdm\\changelogs\\changelog.xml");
    }
}
