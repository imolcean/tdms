package io.github.imolcean.tdms.x.updaters;

import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.api.interfaces.updater.IterativeSchemaUpdater;
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

import java.nio.file.Path;
import java.util.Properties;

@Extension
@Log
@NoArgsConstructor
public class LiquibaseIterativeSchemaUpdater extends IterativeSchemaUpdater
{
    private String changelogPath;

    @SuppressWarnings("unused")
    public LiquibaseIterativeSchemaUpdater(Properties properties)
    {
        this.changelogPath = properties.getProperty("changelog.path");
        this.internalDs = null;
        this.tmpDs = null;

        log.fine("Liquibase changelog: " + (this.changelogPath != null ? this.changelogPath : "unspecified"));
    }

    @Override
    public void setUpdateDescriptor(Path descriptor)
    {
        this.changelogPath = descriptor.toAbsolutePath().toString();
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

        prepare();

        Database internalDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(internalDs.getConnection()));
        Database tmpDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(tmpDs.getConnection()));

        try(Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), tmpDb))
        {
            liquibase.update(new Contexts());

            DiffResult diff = liquibase.diff(internalDb, tmpDb, CompareControl.STANDARD);

            log.info("Update initialised");
            log.info("Preparing schema update report");

            SchemaUpdateReport report = new DiffMapper(schemaService, internalDs, tmpDs).toSchemaUpdate(diff);

            log.info("Report is finished");

            return report;
        }
    }
}
