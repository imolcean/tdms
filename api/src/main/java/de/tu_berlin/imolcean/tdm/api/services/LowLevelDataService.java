package de.tu_berlin.imolcean.tdm.api.services;

import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface LowLevelDataService
{
    void insertRows(Connection connection, Table table, List<Object[]> rows) throws SQLException;

    void clearTable(Connection connection, Table table) throws SQLException;

    void disableConstraints(DataSource ds) throws SQLException, IOException;

    void disableConstraints(Connection connection) throws SQLException, IOException;

    void enableConstraints(DataSource ds) throws SQLException, IOException;

    void enableConstraints(Connection connection) throws SQLException, IOException;

    Connection createTransaction(DataSource ds) throws SQLException;

    void commitTransaction(Connection connection) throws SQLException;

    void rollbackTransaction(Connection connection) throws SQLException;
}
