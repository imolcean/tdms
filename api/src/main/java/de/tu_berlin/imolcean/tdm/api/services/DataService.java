package de.tu_berlin.imolcean.tdm.api.services;

import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataService
{
    int getTableRowCount(DataSource ds, Table table) throws SQLException;

    List<Object[]> getTableContent(DataSource ds, Table table) throws SQLException;

    List<Object> getTableContentForColumn(DataSource ds, Table table, Column column) throws SQLException;

    List<Object[]> getTableContentForColumns(DataSource ds, Table table, List<Column> columns) throws SQLException;

    void insertRows(DataSource ds, Table table, List<Map<Column, Object>> rows) throws SQLException;

    void importData(DataSource ds, Map<Table, List<Object[]>> data) throws SQLException, IOException;

    void updateRow(DataSource ds, Table table, int rowIndex, Map<Column, Object> row) throws SQLException;

    void deleteRow(DataSource ds, Table table, int rowIndex) throws SQLException;

    void copyData(DataSource src, DataSource target, Collection<Table> tables) throws SQLException, IOException;

    boolean isTableEmpty(DataSource ds, Table table) throws SQLException;

    boolean areTablesEmpty(DataSource ds, Collection<Table> tables) throws SQLException;

    void clearTable(DataSource ds, Table table) throws SQLException;

    void clearTables(DataSource ds, Collection<Table> tables) throws SQLException, IOException;
}
