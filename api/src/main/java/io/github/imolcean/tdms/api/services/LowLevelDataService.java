package io.github.imolcean.tdms.api.services;

import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * This service provides a way to read and modify data in the database tables.
 *
 * One distinct feature that differs this service from the {@link DataService} is that
 * this service does not handle transactions by itself and therefore usually works with the {@link Connection} objects.
 */
public interface LowLevelDataService
{
    /**
     * Retrieves all rows from the given table
     *
     * @param connection database containing the {@code table}
     * @param table table whose rows are being retrieved
     * @return list of rows of the given table
     */
    List<Object[]> getTableContent(Connection connection, Table table) throws SQLException;

    /**
     * Retrieves all rows from the given table but the returned rows contain only the specified column.
     *
     * @param connection database containing the {@code table}
     * @param table table whose rows are being retrieved
     * @param column column that should be retrieved
     * @return list of rows of the given table
     */
    List<Object> getTableContentForColumn(Connection connection, Table table, Column column) throws SQLException;

    /**
     * Retrieves all rows from the given table but the returned rows contain only the specified columns.
     *
     * @param connection database containing the {@code table}
     * @param table table whose rows are being retrieved
     * @param columns columns that should be retrieved
     * @return list of rows of the given table
     */
    List<Object[]> getTableContentForColumns(Connection connection, Table table, List<Column> columns) throws SQLException;

    /**
     * Adds new rows into the specified table.
     *
     * @param connection database containing the {@code table}
     * @param table table that receives new rows
     * @param rows data rows that should be added into the table
     */
    void insertRows(Connection connection, Table table, List<Object[]> rows) throws SQLException;

    /**
     * Removes all rows from the specified table.
     *
     * @param connection database containing the {@code table}
     * @param table table that should be cleared
     */
    void clearTable(Connection connection, Table table) throws SQLException;

    /**
     * Disables database constraints.
     * Example: When constraints are disabled, the database won't check for referential integrity of foreign keys.
     *
     * @param ds database whose constraints should be disabled
     */
    void disableConstraints(DataSource ds) throws SQLException, IOException;

    /**
     * Disables database constraints.
     * Example: When constraints are disabled, the database won't check for referential integrity of foreign keys.
     *
     * @param connection database whose constraints should be disabled
     */
    void disableConstraints(Connection connection) throws SQLException, IOException;

    /**
     * Enables database constraints.
     * Example: When constraints are enabled, the database will check for referential integrity of foreign keys.
     *
     * @param ds database whose constraints should be enabled
     */
    void enableConstraints(DataSource ds) throws SQLException, IOException;

    /**
     * Enables database constraints.
     * Example: When constraints are enabled, the database will check for referential integrity of foreign keys.
     *
     * @param connection database whose constraints should be enabled
     */
    void enableConstraints(Connection connection) throws SQLException, IOException;

    /**
     * A {@link Connection} object will be created and its {@code autoCommit} flag is set to {@code false}.
     *
     * @param ds database that a transaction should be created for
     * @return {@link Connection} representing a new transaction
     */
    Connection createTransaction(DataSource ds) throws SQLException;

    /**
     * Commits the given transaction.
     *
     * @param connection transaction that should be committed
     */
    void commitTransaction(Connection connection) throws SQLException;

    /**
     * Rolls back the given transaction.
     *
     * @param connection transaction that should be rolled back
     */
    void rollbackTransaction(Connection connection) throws SQLException;
}
