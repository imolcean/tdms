package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.DiffSchemaUpdater;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.pf4j.Extension;

import java.util.Properties;

@Extension
@Log
@NoArgsConstructor
public class LiquibaseDiffSchemaUpdater extends DiffSchemaUpdater
{
    private String changelogPath;

    @SuppressWarnings("unused")
    public LiquibaseDiffSchemaUpdater(Properties properties)
    {
        this.changelogPath = properties.getProperty("changelog.path");
        this.dataMapped = false;
        this.internalDs = null;
        this.tmpDs = null;

        if(this.changelogPath == null)
        {
            throw new IllegalStateException("Changelog path is not configured");
        }

        log.fine("Liquibase changelog: " + this.changelogPath);
    }

    @Override
    public SchemaUpdateReport initSchemaUpdate(DataSourceWrapper internalDs, DataSourceWrapper tmpDs) throws Exception
    {
        if(isUpdateInProgress())
        {
            throw new IllegalStateException("Another schema update is already in progress");
        }

        if(changelogPath == null)
        {
            throw new IllegalStateException("Changelog path is not configured");
        }

        this.internalDs = internalDs;
        this.tmpDs = tmpDs;

        log.info("Initialising schema update");

        Database internalDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(internalDs.getConnection()));
        Database tmpDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(tmpDs.getConnection()));

        try(Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), tmpDb))
        {
            liquibase.dropAll(); // Clearing Temp DB, just in case
            liquibase.update(new Contexts());

            // We don't use incremental updates here so we also don't need these tables
            schemaService.dropTable(tmpDs, "DATABASECHANGELOG");
            schemaService.dropTable(tmpDs, "DATABASECHANGELOGLOCK");

            DiffResult diff = liquibase.diff(internalDb, tmpDb, CompareControl.STANDARD);

            log.info("Update initialised");
            log.info("Preparing schema update report");

            SchemaUpdateReport report = new DiffMapper(schemaService, internalDs, tmpDs).toSchemaUpdate(diff);

            log.info("Report is finished");

            return report;
        }
    }
}
