package de.tu_berlin.imolcean.tdm.api.interfaces.updater;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.exceptions.TableNotFoundException;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a special case of {@link SchemaUpdater} when the schema updates are not meant to be handled
 * as a list of successive changes.
 *
 * Every update is carrying a complete schema description that is applied to the empty Temp DB. Next, a difference
 * between two schemas (Internal DB and Temp DB) is built. For every untouched table (i.e. table that has the same
 * column names, data types, constraints, etc.), its data will be copied to the new schema automatically. Every other
 * table needs an SQL migration script. These scripts should be provided by the user when calling the
 * {@code commitSchemaUpdate} method.
 */
@Log
public abstract class SimpleSchemaUpdater extends AbstractSchemaUpdater
{
    /**
     * Maps all data from the old schema to the new schema.
     *
     * This method determines which tables should be mapped automatically and which ones do not.
     * In case something goes wrong during the process, the whole mapping will be rolled back,
     * leaving the Temp DB in the state it was before.
     *
     * @param request describes how data from the old schema will be mapped to the new one
     * @throws SQLException if something goes wrong while interacting with the database
     * @throws SchemaCrawlerException if something goes wrong while extracting schema information
     */
    protected void mapData(SchemaUpdateCommitRequest request)
            throws SQLException, SchemaCrawlerException
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
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
            }
            catch(Exception e)
            {
                log.warning("Data mapping failed");

                tmpDsConnection.rollback();
                throw e;
            }

            tmpDsConnection.commit();
        }

        log.info("Data mapping finished");
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

    private void mapDataManually(Connection connection, SchemaUpdateCommitRequest.TableDataMigrationRequest request) throws SQLException
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
