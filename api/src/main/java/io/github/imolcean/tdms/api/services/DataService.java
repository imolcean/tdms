package io.github.imolcean.tdms.api.services;

import io.github.imolcean.tdms.api.exceptions.TableContentRowIndexOutOfBoundsException;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This service provides a way to read and modify data in the database tables.
 *
 * One distinct feature that differs this service from the {@link LowLevelDataService} is that
 * this service handles every action as a separate transaction an therefore works directly with the
 * {@link DataSource} objects.
 */
public interface DataService
{
    /**
     * Counts rows in the given table.
     *
     * @param ds database containing the {@code table}
     * @param table table whose rows are being counted
     * @return number of rows in the table
     */
    int getTableRowCount(DataSource ds, Table table) throws SQLException;

    /**
     * Retrieves all rows from the given table
     *
     * @param ds database containing the {@code table}
     * @param table table whose rows are being retrieved
     * @return list of rows of the given table
     */
    List<Object[]> getTableContent(DataSource ds, Table table) throws SQLException;

    /**
     * Retrieves all rows from the given table but the returned rows contain only the specified column.
     *
     * @param ds database containing the {@code table}
     * @param table table whose rows are being retrieved
     * @param column column that should be retrieved
     * @return list of rows of the given table
     */
    List<Object> getTableContentForColumn(DataSource ds, Table table, Column column) throws SQLException;

    /**
     * Retrieves all rows from the given table but the returned rows contain only the specified columns.
     *
     * @param ds database containing the {@code table}
     * @param table table whose rows are being retrieved
     * @param columns columns that should be retrieved
     * @return list of rows of the given table
     */
    List<Object[]> getTableContentForColumns(DataSource ds, Table table, List<Column> columns) throws SQLException;

    /**
     * Adds new rows into the specified table.
     * If insertion of at least one row fails, the whole transaction will be rolled back.
     *
     * @param ds database containing the {@code table}
     * @param table table that receives new rows
     * @param rows data rows that should be added into the table
     */
    void insertRows(DataSource ds, Table table, List<Map<Column, Object>> rows) throws SQLException;

    /**
     * Imports new rows into the specified table.
     * If insertion of at least one row fails, the whole transaction will be rolled back.
     *
     * @param ds database containing the {@code table}
     * @param data map of tables on the data that should be imported in that tables
     */
    void importData(DataSource ds, Map<Table, List<Object[]>> data) throws SQLException, IOException;

    /**
     * Modifies the content of the specified row.
     *
     * @param ds database containing the {@code table}
     * @param table table whose row should be modified
     * @param rowIndex ordinal number of the row that should be modified
     *                 (starts with 0, should be less than the number returned by the method {@code getTableRowCount})
     * @param row map of the columns that should change on the new values of these columns
     * @throws TableContentRowIndexOutOfBoundsException if {@code rowIndex} is invalid
     */
    void updateRow(DataSource ds, Table table, int rowIndex, Map<Column, Object> row) throws SQLException;

    /**
     * Removes the row with the specified index from the table.
     *
     * @param ds database containing the {@code table}
     * @param table table whose row is being removed
     * @param rowIndex ordinal number of the row that should be removed
     *                 (starts with 0, should be less than the number returned by the method {@code getTableRowCount})
     * @throws TableContentRowIndexOutOfBoundsException if {@code rowIndex} is invalid
     */
    void deleteRow(DataSource ds, Table table, int rowIndex) throws SQLException;

    /**
     * Copies all rows of all tables from the {@code src} database into the {@code target} database.
     * The schemas of both tables should be identical for this method to work correctly.
     *
     * @param src database whose data is being copied
     * @param target database that receives the copied data
     * @param tables tables whose content should be copied
     */
    void copyData(DataSource src, DataSource target, Collection<Table> tables) throws SQLException, IOException;

    /**
     * Check if the specified table is empty, i.e. contains no rows.
     *
     * @param ds database containing the {@code table}
     * @param table table that is being checked
     * @return true if the specified table contains no rows, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isTableEmpty(DataSource ds, Table table) throws SQLException;

    /**
     * Checks if all specified tables are empty, i.e. contain no rows.
     *
     * @param ds database containing the {@code tables}
     * @param tables tables that are being checked
     * @return true if all specified tables are empty, false otherwise
     */
    boolean areTablesEmpty(DataSource ds, Collection<Table> tables) throws SQLException;

    /**
     * Removes all rows from the specified table.
     * In case the removal of at least one row fails (due to database constraints, for example), the
     * whole transaction will be rolled back.
     *
     * @param ds database containing the {@code table}
     * @param table table that should be cleared
     */
    void clearTable(DataSource ds, Table table) throws SQLException;

    /**
     * Removes all rows from the specified tables.
     * In case the removal of at least one row fails (due to database constraints, for example), the
     * whole transaction will be rolled back.
     *
     * @param ds database containing the {@code tables}
     * @param tables tables that should be cleared
     */
    void clearTables(DataSource ds, Collection<Table> tables) throws SQLException, IOException;
}
