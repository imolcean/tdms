package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.x.DiffMapper;
import liquibase.Contexts;
import liquibase.LabelExpression;
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
import java.sql.Connection;
import java.util.Properties;

@Extension
@Log
@NoArgsConstructor
public class LiquibaseUpdater implements SchemaUpdater
{
    private SchemaService schemaService;

    private String changelogPath;

    // TODO Store intermediate DB info in plugin's config?
    // TODO Use tmp schema instead of tmp DB?
    // TODO Insert SchemaService through DI
    @SuppressWarnings("unused")
    public LiquibaseUpdater(Properties properties)
    {
        this.changelogPath = properties.getProperty("changelog.path");

        log.fine("Liquibase changelog: " + this.changelogPath);
    }

    @Override
    public SchemaUpdateReport initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        if(changelogPath == null)
        {
            throw new IllegalStateException("Changelog path is not configured");
        }

        log.info("Initialising schema update");

        try(Connection internalDsConnection = internalDs.getConnection();
            Connection tmpDsConnection = tmpDs.getConnection())
        {
            Database internalDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(internalDsConnection));
            Database tmpDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(tmpDsConnection));

            try(Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), tmpDb))
            {
                liquibase.dropAll();
                liquibase.update(new Contexts(), new LabelExpression());

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

    @Override
    public void setSchemaService(SchemaService service)
    {
        this.schemaService = service;
    }
}
