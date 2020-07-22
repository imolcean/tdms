package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.exceptions.TableNotFoundException;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
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
import org.apache.logging.log4j.util.Strings;
import org.pf4j.Extension;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Extension
@Log
@NoArgsConstructor
public class LiquibaseUpdater implements SchemaUpdater
{
    private SchemaService schemaService;

    private String changelogPath;

    private DataSource internalDs;
    private DataSource tmpDs;

    // TODO Pack Liquibase.Core in the plugin jar
    // TODO Store intermediate DB info in plugin's config?
    // TODO Insert SchemaService
    @SuppressWarnings("unused")
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

                return new DiffMapper(schemaService, internalDs, tmpDs).toSchemaUpdate(diff);
            }
        }
    }

    @Override
    // TODO Test
    public void commitSchemaUpdate(SchemaUpdateCommitRequest request) throws Exception
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Committing schema update");

        try(Connection internalDsConnection = this.internalDs.getConnection();
            Connection tmpDsConnection = this.tmpDs.getConnection())
        {
            internalDsConnection.setAutoCommit(false);
            tmpDsConnection.setAutoCommit(false);

            // Copy data from internalDs -> tmpDs for every table that has no provided SQL migration script
            // Empty tables are ignored for performance reason

            log.info("Migrating tables automatically");

            log.fine("Choosing tables for auto migration");

            List<String> allTablesBeforeUpdate = schemaService.getTableNames(internalDs);
            List<String> allTablesAfterUpdate = schemaService.getTableNames(tmpDs);
            List<String> emptyTables = schemaService.getEmptyTableNames(internalDs);
            List<String> tablesToMigrateManually = request.getSqlMigrationTables().stream()
                    .map(SchemaUpdateCommitRequest.TableDataMigrationRequest::getTableName)
                    .collect(Collectors.toList());

            @SuppressWarnings("Convert2MethodRef")
            List<String> tablesToMigrateAutomatically = allTablesAfterUpdate.stream()
                    .filter(table -> allTablesBeforeUpdate.contains(table))
                    .filter(table -> !tablesToMigrateManually.contains(table))
                    .filter(table -> !emptyTables.contains(table))
                    .collect(Collectors.toList());

            log.fine(String.format("%s tables chosen", tablesToMigrateAutomatically.size()));

            for(String tableName : tablesToMigrateAutomatically)
            {
                Table internal = schemaService.getTable(internalDs, tableName);
                Table tmp = schemaService.getTable(tmpDs, tableName);

                migrateDataAutomatically(tmpDsConnection, internal, tmp);
            }

            log.info("Automatic migration finished");


            // Perform SQL from SchemaUpdateCommitRequest for every mentioned table

            log.info("Migrating tables using provided migration scripts");

            for(SchemaUpdateCommitRequest.TableDataMigrationRequest req : request.getSqlMigrationTables())
            {
                if(!allTablesAfterUpdate.contains(req.getTableName()))
                {
                    throw new TableNotFoundException(req.getTableName());
                }

                migrateDataManually(tmpDsConnection, req);
            }

            log.info("Migration using scripts finished");


            // Commit: purge internalDs, migrate tmpDs -> internalDs, purge tmpDs

            log.info("Committing");

            log.fine("Purging Internal DB");

            // TODO

            log.fine("Moving schema and data from Temp DB into Internal DB");

            // TODO

            log.fine("Purging Temp DB");

            // TODO


            internalDsConnection.commit();
            tmpDsConnection.commit();

            internalDsConnection.setAutoCommit(true);
            tmpDsConnection.setAutoCommit(true);
        }
        catch(Exception e)
        {
            log.severe("Schema update commit failed");
            throw e;
        }

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

    private void migrateDataAutomatically(Connection connection, Table src, Table target) throws SQLException
    {
        String srcTableName = src.getFullName();
        String targetTableName = target.getFullName();
        String columnNames = src.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) SELECT %s FROM %s", targetTableName, columnNames, columnNames, srcTableName);

        log.fine(String.format("%s: %s", srcTableName, sql));

        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
        }
    }

    private void migrateDataManually(Connection connection, SchemaUpdateCommitRequest.TableDataMigrationRequest request)
            throws SQLException
    {
        log.fine(String.format("%s: %s", request.getTableName(), request.getSql()));

        if(Strings.isBlank(request.getSql()))
        {
            return;
        }

        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(request.getSql());
        }
    }
}
