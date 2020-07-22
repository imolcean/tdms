package de.tu_berlin.imolcean.tdm.api.services;

import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public interface TableContentService
{
    int getTableRowCount(DataSource ds, Table table) throws SQLException;

    List<Object[]> getTableContent(DataSource ds, Table table) throws SQLException;

    void insertRow(DataSource ds, Table table, Object[] row) throws SQLException;

    void updateRow(DataSource ds, Table table, int rowIndex, Object[] row) throws SQLException;

    void deleteRow(DataSource ds, Table table, int rowIndex) throws SQLException;

    int countTableContentRowReferences(DataSource ds, Table table, Object[] row);
}