package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
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
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Relation;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.pf4j.Extension;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

// TODO Pack Liquibase.Core in the plugin jar
// TODO Store intermediate DB info in plugin's config?
// TODO Use no second DB

@Extension
@Log
@NoArgsConstructor
public class LiquibaseUpdater implements SchemaUpdater
{
    private SchemaService schemaService;

    private String changelogPath;

    private DataSource internalDs;
    private DataSource tmpDs;

    // TODO Insert SchemaService
    public LiquibaseUpdater(Properties properties)
    {
        this.changelogPath = properties.getProperty("changelog.path");
        this.internalDs = null;
        this.tmpDs = null;

        log.fine("Liquibase changelog: " + this.changelogPath);
    }

    @Override
    public SchemaUpdate initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        if(changelogPath == null)
        {
            throw new IllegalStateException("Changelog path is not configured");
        }

        if(isUpdateInProgress())
        {
            throw new IllegalStateException("Another schema update is already in progress");
        }

        log.info("Initialising schema update");

        // TODO Use tmp schema instead of tmp DB?

        this.internalDs = internalDs;
        this.tmpDs = tmpDs;

        try(Connection internalDsConnection = this.internalDs.getConnection();
            Connection tmpDsConnection = this.tmpDs.getConnection())
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

                return diff2SchemaUpdateDto(diff);
            }
        }
    }

    @Override
    public void commitSchemaUpdate(SchemaUpdate update)
    {
        log.info("Committing schema update");

        // TODO Copy data from intermediate db into ds for every table with empty diff
        // TODO Perform SQL from SchemaUpdateDto

        this.tmpDs = null;
        this.internalDs = null;

        log.info("Schema update committed");
    }

    @Override
    public void cancelSchemaUpdate() throws Exception
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Cancelling schema update");

        try(Connection connection = tmpDs.getConnection())
        {
            Database tmpDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try(Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), tmpDb))
            {
                liquibase.dropAll();
            }
        }

        this.tmpDs = null;
        this.internalDs = null;

        log.info("Schema update cancelled");
    }

    @Override
    public boolean isUpdateInProgress()
    {
        return tmpDs != null && internalDs != null;
    }

    @Override
    public void setSchemaService(SchemaService service)
    {
        this.schemaService = service;
    }

    private SchemaUpdate diff2SchemaUpdateDto(DiffResult diff) throws SQLException, SchemaCrawlerException
    {
        List<String> untouchedTables = schemaService.getTableNames(internalDs);

        List<Table> addedTables = new ArrayList<>();
        List<Table> deletedTables = new ArrayList<>();
        List<SchemaUpdate.Comparison> changedTables = new ArrayList<>();

        for(liquibase.structure.core.Table obj : diff.getUnexpectedObjects(liquibase.structure.core.Table.class))
        {
            addedTables.add(schemaService.getTable(tmpDs, obj.getName()));
        }

        for(liquibase.structure.core.Table obj : diff.getMissingObjects(liquibase.structure.core.Table.class))
        {
            deletedTables.add(schemaService.getTable(internalDs, obj.getName()));
            untouchedTables.remove(obj.getName());
        }

        // TODO Changed tables are either renamed ones or ones that have added/removed/changed columns or column attributes
        for(DatabaseObject obj : diff.getChangedObjects(liquibase.structure.core.Table.class).keySet())
        {
            Table before = schemaService.getTable(internalDs, obj.getName());
            Table after = schemaService.getTable(tmpDs, obj.getName());

            changedTables.add(new SchemaUpdate.Comparison(before, after));
            untouchedTables.remove(obj.getName());
        }

        return new SchemaUpdate(untouchedTables, addedTables, deletedTables, changedTables);
    }
}
