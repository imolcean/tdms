package io.github.imolcean.tdms.api.interfaces.updater;

import io.github.imolcean.tdms.api.dto.SchemaUpdateDataMappingRequest;
import io.github.imolcean.tdms.api.exceptions.TableNotFoundException;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a special case of {@link SchemaUpdater} when the schema updates are not meant to be handled
 * as a list of successive changes but rather as a difference between the old and new schemas.
 *
 * Every update is carrying a complete schema description that is applied to the empty Temp DB. Next, a difference
 * between two schemas (Internal DB and Temp DB) is built. For every untouched table (i.e. table that has the same
 * column names, data types, constraints, etc.), its data will be copied to the new schema automatically. Every other
 * table needs an SQL migration script. These scripts should be provided by the user by calling the
 * {@code mapData} method.
 */
@Log
public abstract class DiffSchemaUpdater extends AbstractSchemaUpdater
{
    protected boolean dataMapped;

    @Override
    public void mapData(SchemaUpdateDataMappingRequest request) throws SQLException, SchemaCrawlerException
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        if(dataMapped)
        {
            throw new IllegalStateException("Data mapping has already been performed");
        }

        log.info("Mapping data to the new schema");

        try(Connection tmpDsConnection = tmpDs.getConnection())
        {
            tmpDsConnection.setAutoCommit(false);

            try
            {
                // Copy data from internalDs -> tmpDs for every table that has no provided SQL migration script
                // Empty tables are ignored for performance reason

                log.info("Mapping tables automatically");
                log.fine("Choosing tables for auto mapping");

                List<String> allTablesBeforeUpdate = schemaService.getTableNames(internalDs);
                List<String> allTablesAfterUpdate = schemaService.getTableNames(tmpDs);
                List<String> emptyTables = schemaService.getEmptyTableNames(internalDs);
                List<String> tablesToMigrateManually = request.getSqlMigrationTables().stream()
                        .map(SchemaUpdateDataMappingRequest.TableDataMigrationRequest::getTableName)
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

                for(SchemaUpdateDataMappingRequest.TableDataMigrationRequest req : request.getSqlMigrationTables())
                {
                    if(!allTablesAfterUpdate.contains(req.getTableName()))
                    {
                        throw new TableNotFoundException(req.getTableName());
                    }

                    mapDataManually(tmpDsConnection, req);
                }

                log.info("Mapping using scripts finished");
            }
            catch(Exception e)
            {
                log.warning("Data mapping failed");

                tmpDsConnection.rollback();
                throw e;
            }

            tmpDsConnection.commit();
        }

        dataMapped = true;

        log.info("Data mapping finished");
    }

    @Override
    public void rollbackDataMapping() throws SQLException, SchemaCrawlerException, IOException
    {
        if(!dataMapped)
        {
            throw new IllegalStateException("Data mapping has not yet been performed");
        }

        log.info("Rolling back data mapping");

        dataService.clearTables(tmpDs, schemaService.getSchema(tmpDs).getTables());

        dataMapped = false;

        log.info("Data mapping rolled back");
    }

    private void mapDataAutomatically(Connection connection, Table src, Table target) throws SQLException
    {
        String columnNames = src.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) SELECT %s FROM %s",
                target.getFullName(),
                columnNames,
                columnNames,
                src.getFullName());

        log.fine(String.format("%s: %s", src.getFullName(), sql));

        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
        }
    }

    private void mapDataManually(Connection connection, SchemaUpdateDataMappingRequest.TableDataMigrationRequest request) throws SQLException
    {
        log.fine(String.format("%s: %s", request.getTableName(), request.getSql()));

        if(Strings.isBlank(request.getSql()))
        {
            return;
        }

        String sql = request.getSql()
                .replaceAll("(?i)old", internalDs.getDatabase())
                .replaceAll("(?i)new", tmpDs.getDatabase());

        log.fine(sql);

        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
        }
    }
}
