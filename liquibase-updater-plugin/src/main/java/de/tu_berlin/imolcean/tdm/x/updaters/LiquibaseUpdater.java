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
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.pf4j.Extension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
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
    private DataSource internalDs;
    private DataSource tmpDs;

    public LiquibaseUpdater(Properties properties)
    {
        this.changelogPath = properties.getProperty("changelog.path");
        this.internalDs = null;
        this.tmpDs = null;

        log.fine("Liquibase changelog: " + this.changelogPath);
    }

    @Override
    public SchemaUpdateDto initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
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

                System.out.println("Created:");
                for(DatabaseObject obj : diff.getUnexpectedObjects(Table.class))
                {
                    System.out.println(String.format("%s (%s)", obj.getName(), obj.getObjectTypeName()));
                }

                log.info("Update initialised");

                return diff2SchemaUpdateDto(diff);
            }
        }
    }

    @Override
    public void commitSchemaUpdate(SchemaUpdateDto update)
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

    private SchemaUpdateDto diff2SchemaUpdateDto(DiffResult diff)
    {
        List<String> addedTables = new ArrayList<>();
        List<String> changedTables = new ArrayList<>();
        List<String> deletedTables = new ArrayList<>();

        for(DatabaseObject obj : diff.getUnexpectedObjects(Table.class))
        {
            addedTables.add(obj.getName());
        }

        for(DatabaseObject obj : diff.getChangedObjects(Table.class).keySet())
        {
            changedTables.add(obj.getName());
        }

        for(DatabaseObject obj : diff.getMissingObjects(Table.class))
        {
            deletedTables.add(obj.getName());
        }

        return new SchemaUpdateDto(addedTables, changedTables, deletedTables);
    }
}
