package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDto;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.report.DiffToReport;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.pf4j.Extension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

// TODO Pack Liquibase.Core in the plugin jar
// TODO Store intermediate DB info in plugin's config?
// TODO Use no second DB

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
    public SchemaUpdateDto initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        log.fine("Initialising schema update");

        if(changelogPath == null)
        {
            throw new IllegalStateException("Changelog path is not configured");
        }

        try(Connection internalDsConnection = internalDs.getConnection(); Connection tmpDsConnection = tmpDs.getConnection())
        {
            Database internalDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(internalDsConnection));
            Database tmpDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(tmpDsConnection));

            try(Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), tmpDb))
            {
                liquibase.update(new Contexts(), new LabelExpression());

                DiffResult diff = liquibase.diff(internalDb, tmpDb, CompareControl.STANDARD);

                // TODO Remove
                new DiffToReport(diff, System.out).print();
            }
        }

        log.fine("Update initialised");

        // TODO Return SchemaUpdateDto
        return null;
    }

    @Override
    public void commitSchemaUpdate(DataSource internalDs, DataSource tmpDs, SchemaUpdateDto update)
    {
        log.fine("Committing schema update");

        // TODO Copy data from intermediate db into ds for every table with empty diff
        // TODO Perform SQL from SchemaUpdateDto

        log.fine("Schema update committed");
    }
}
