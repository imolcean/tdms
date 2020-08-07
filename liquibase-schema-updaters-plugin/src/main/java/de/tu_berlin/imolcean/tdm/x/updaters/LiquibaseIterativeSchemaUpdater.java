package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.interfaces.updater.IterativeSchemaUpdater;
import de.tu_berlin.imolcean.tdm.x.DiffMapper;
import liquibase.Contexts;
import liquibase.Liquibase;
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

        // TODO Fail here if path == null

        log.fine("Liquibase changelog: " + this.changelogPath);
    }

    @Override
    public SchemaUpdateReport initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
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

            // TODO Remove
            new DiffToReport(diff, System.out).print();

            log.info("Update initialised");
            log.info("Preparing schema update report");

            SchemaUpdateReport report = new DiffMapper(schemaService, internalDs, tmpDs).toSchemaUpdate(diff);

            log.info("Report is finished");

            return report;
        }
    }
}
