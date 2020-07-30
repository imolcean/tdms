package de.tu_berlin.imolcean.tdm.api.services;

import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface TableContentService
{
    int getTableRowCount(DataSource ds, Table table) throws SQLException;

    List<Object[]> getTableContent(DataSource ds, Table table) throws SQLException;

    @Deprecated
    void insertRow(DataSource ds, Table table, Object[] row) throws SQLException;

    void insertRows(DataSource ds, Table table, List<Object[]> rows) throws SQLException;

    void updateRow(DataSource ds, Table table, int rowIndex, Object[] row) throws SQLException;

    void deleteRow(DataSource ds, Table table, int rowIndex) throws SQLException;

    void copyData(DataSource src, DataSource target, Collection<Table> tables) throws SQLException, IOException;

    void clearTable(DataSource ds, Table table) throws SQLException;

    void clearTables(DataSource ds, Collection<Table> tables) throws SQLException, IOException;
}
