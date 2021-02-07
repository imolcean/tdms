package io.github.imolcean.tdms.api.services;

import io.github.imolcean.tdms.api.exceptions.TableNotFoundException;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This service provides methods for working with the database schema.
 *
 * It allows retrieving list of all tables in a given {@link DataSource} as well as
 * copying schema from one {@link DataSource} to another or even drop all the tables completely.
 *
 * Under the hood, this service is meant to use Schema Crawler, so its exceptions might be thrown.
 */
public interface SchemaService
{
    /**
     * Retrieves the whole schema of a database with all the tables, indexes, etc.
     *
     * This method may take some time when called for databases with large schemas,
     * so implementations might (but do not have to) use caching to spare time for repeated calls. However, the first call
     * will still be expensive.
     *
     * Caution: Please try to avoid calling this method because it is blocking and may take long time on large schemas.
     *
     * @param ds Database whose schema is being retrieved
     * @return Whole schema of the given database
     */
    Catalog getSchema(DataSource ds) throws SQLException, SchemaCrawlerException;

    /**
     * Creates an exact copy of the schema of the {@code src} database in the {@code target} database.
     * This method is only guaranteed to work correctly when the {@code target} database contains an empty schema.
     *
     * @param src database whose schema is being copied
     * @param target database that receives a new schema, should be empty before calling this method
     */
    void copySchema(DataSource src, DataSource target) throws Exception;

    /**
     * Clears the schema of the given database, i.e. removes all its tables.
     * This method might fail if, for example, some tables may not be removed due to some database constraints.
     *
     * @param ds database that is being cleared
     */
    void purgeSchema(DataSource ds) throws Exception;

    /**
     * Lists names of all the tables found in the given database.
     *
     * @param ds database whose tables are being listed
     * @return list of all table names
     */
    List<String> getTableNames(DataSource ds) throws SQLException, SchemaCrawlerException;

    /**
     * Lists names of all non-empty tables found in the given database.
     *
     * @param ds database whose tables are being listed
     * @return list of all non-empty table names
     */
    List<String> getOccupiedTableNames(DataSource ds) throws SQLException, SchemaCrawlerException;

    /**
     * Lists names of all empty tables found in the given database.
     *
     * @param ds database whose tables are being listed
     * @return list of all empty table names
     */
    List<String> getEmptyTableNames(DataSource ds) throws SQLException, SchemaCrawlerException;

    /**
     * Finds a table by its names in the given database.
     *
     * @param ds database that contains the table
     * @param tableName name of the table that is being searched
     * @return Table with the given name
     * @throws TableNotFoundException If the database doesn't contain a table with the given name
     */
    Table getTable(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException;

    /**
     * Finds tables by their names in the given database.
     *
     * @param ds database that contains the tables
     * @param tableNames names of the tables that are being searched
     * @return Tables with the given names
     * @throws TableNotFoundException in case at least one of the tables could not be found
     */
    List<Table> getTables(DataSource ds, Collection<String> tableNames) throws SQLException, SchemaCrawlerException;

    /**
     * Finds column by its name in the given table
     *
     * @param table table that the column is being searched in
     * @param columnName name of the column that is being searched
     * @return Optional containing the column if it could be found, empty Optional otherwise
     */
    Optional<Column> findColumn(Table table, String columnName);

    /**
     * Checks if a table with the given name is present in the given database.
     *
     * @param ds database that is being looked in
     * @param tableName name of the table that is being searched
     * @return true if the table exists, false otherwise
     */
    boolean tableExists(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException;

    /**
     * Removes a table by its name from the given database.
     * This method might fail if, for example, the table may not be removed due to some database constraints.
     *
     * @param ds database that contains the table
     * @param tableName name of the table that should be removed
     */
    void dropTable(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException;
}
