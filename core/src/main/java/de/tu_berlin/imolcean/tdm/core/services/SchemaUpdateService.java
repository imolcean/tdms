package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.exceptions.TableNotFoundException;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.DataAwareSchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log
public class SchemaUpdateService
{
    // TODO Test
    // TODO JavaDoc

    private final SchemaUpdateImplementationManager schemaUpdateImplementationManager;
    private final SchemaService schemaService;

    private DataSource internalDs;
    private DataSource tmpDs;

    public SchemaUpdateService(SchemaUpdateImplementationManager schemaUpdateImplementationManager,
                               SchemaService schemaService)
    {
        this.schemaUpdateImplementationManager = schemaUpdateImplementationManager;
        this.schemaService = schemaService;
        this.internalDs = null;
        this.tmpDs = null;
    }

    public boolean isUpdateInProgress()
    {
        return tmpDs != null && internalDs != null;
    }

    public SchemaUpdater.SchemaUpdateReport initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        if(isUpdateInProgress())
        {
            throw new IllegalStateException("Another schema update is already in progress");
        }

        this.internalDs = internalDs;
        this.tmpDs = tmpDs;

        return getSchemaUpdater().initSchemaUpdate(internalDs, tmpDs);
    }

    public void commitSchemaUpdate(SchemaUpdateCommitRequest request) throws Exception
    {
        if(getSchemaUpdater() instanceof DataAwareSchemaUpdater)
        {
            log.info("Skipping data migration because it was handled by the schema updater");
        }
        else
        {
            mapData(request);
        }

        log.info("Committing schema update");
        log.fine("Purging Internal DB");

        // TODO
//        try(Connection connection = internalDs.getConnection())
//        {
//            Database tmpDb = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
//
//            try(Liquibase liquibase = new Liquibase("", new FileSystemResourceAccessor(), tmpDb))
//            {
//                liquibase.dropAll();
//            }
//        }

        log.fine("Moving schema and data from Temp DB into Internal DB");

        // TODO
//        try(Connection tmpDsConnection = tmpDs.getConnection();
//            Connection internalDsConnection = internalDs.getConnection())
//        {
//            // TODO Recreate schema manually if needed
//
//            Database tmpDb = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(tmpDsConnection));
//
//            Database internalDb = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(internalDsConnection));
//
//            try(Liquibase liquibase = new Liquibase("", new FileSystemResourceAccessor(), tmpDb))
//            {
//                // TODO
//                liquibase.generateChangeLog();
//            }
//        }

        log.fine("Purging Temp DB");

        // TODO
//        try(Connection connection = tmpDs.getConnection())
//        {
//            Database tmpDb = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
//
//            try(Liquibase liquibase = new Liquibase("", new FileSystemResourceAccessor(), tmpDb))
//            {
//                liquibase.dropAll();
//            }
//        }

        log.info("Schema update committed");

        this.tmpDs = null;
        this.internalDs = null;
    }

    public void cancelSchemaUpdate() throws Exception
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Cancelling schema update");

        // TODO Clear tmpDs
//        try(Connection connection = tmpDs.getConnection())
//        {
//            Database tmpDb = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
//
//            try(Liquibase liquibase = new Liquibase("", new FileSystemResourceAccessor(), tmpDb))
//            {
//                liquibase.dropAll();
//            }
//        }

        this.tmpDs = null;
        this.internalDs = null;

        log.info("Schema update cancelled");
    }

    private void mapData(SchemaUpdateCommitRequest request) throws SQLException, SchemaCrawlerException
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Mapping data to the new schema");

        try(Connection tmpDsConnection = tmpDs.getConnection())
        {
            tmpDsConnection.setAutoCommit(false);

            // Copy data from internalDs -> tmpDs for every table that has no provided SQL migration script
            // Empty tables are ignored for performance reason

            log.info("Mapping tables automatically");
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

                mapDataAutomatically(tmpDsConnection, internal, tmp);
            }

            log.info("Automatic mapping finished");


            // Perform SQL from SchemaUpdateCommitRequest for every mentioned table

            log.info("Mapping tables using provided mapping scripts");

            for(SchemaUpdateCommitRequest.TableDataMigrationRequest req : request.getSqlMigrationTables())
            {
                if(!allTablesAfterUpdate.contains(req.getTableName()))
                {
                    throw new TableNotFoundException(req.getTableName());
                }

                mapDataManually(tmpDsConnection, req);
            }

            log.info("Mapping using scripts finished");

            tmpDsConnection.commit();
        }
        catch(Exception e)
        {
            log.severe("Data mapping failed");
            throw e;
        }

        log.info("Data mapping finished");
    }

    private void mapDataAutomatically(Connection connection, Table src, Table target)
            throws SQLException
    {
        String srcTableName = src.getFullName();
        String targetTableName = target.getFullName();
        String columnNames = src.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) SELECT %s FROM %s",
                targetTableName,
                columnNames,
                columnNames,
                srcTableName);

        log.fine(String.format("%s: %s", srcTableName, sql));

        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
        }
    }

    private void mapDataManually(Connection connection, SchemaUpdateCommitRequest.TableDataMigrationRequest request)
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

    private SchemaUpdater getSchemaUpdater()
    {
        return schemaUpdateImplementationManager
                .getSelectedImplementation()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);
    }
}
