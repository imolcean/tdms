package de.tu_berlin.imolcean.tdm.core.services;

import de.danielbechler.util.Strings;
import de.tu_berlin.imolcean.tdm.api.services.LowLevelDataService;
import de.tu_berlin.imolcean.tdm.core.TableContentResultSetHandler;
import de.tu_berlin.imolcean.tdm.core.utils.QueryLoader;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.Column;
import schemacrawler.schema.JavaSqlType;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log
public class DefaultLowLevelDataService implements LowLevelDataService
{
    @Override
    public List<Object[]> getTableContent(Connection connection, Table table) throws SQLException
    {
        log.info("Retrieving content of the table " + table.getName());

        try(Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            log.info("Content retrieved");

            return new TableContentResultSetHandler().handle(rs);
        }
    }

    @Override
    public List<Object[]> getTableContentForColumns(Connection connection, Table table, List<Column> columns) throws SQLException
    {
        if(columns.isEmpty())
        {
            log.warning("No columns specified");
            return Collections.emptyList();
        }

        String columnsStr = columns.stream()
                .map(NamedObject::getName)
                .collect(Collectors.joining(", "));

        log.info(String.format("Retrieving content of the table %s, columns %s", table.getName(), columnsStr));

        try(Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT " + columnsStr + " FROM " + table.getName());

            log.info("Content retrieved");

            return new TableContentResultSetHandler().handle(rs);
        }
    }

    @Override
    public void insertRows(Connection connection, Table table, List<Object[]> rows) throws SQLException
    {
        log.info("Inserting multiple new rows into table " + table.getName());

        List<String> columnNames = table.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.toList());

        List<JavaSqlType> columnTypes = table.getColumns().stream()
                .map(col -> col.getColumnDataType().getJavaSqlType())
                .collect(Collectors.toList());

        String placeholders = new StringBuilder()
                .append("?,".repeat(columnNames.size()))
                .reverse()
                .deleteCharAt(0)
                .toString();

        String insertSql = String
                .format("INSERT INTO %s (%s) VALUES (%s)",
                        table.getName(),
                        Strings.join(", ", columnNames),
                        placeholders);

        log.fine("Request template: " + insertSql);

        try(PreparedStatement statement = connection.prepareStatement(insertSql))
        {
            for(Object[] row : rows)
            {
                statement.clearParameters();

                int i = 0;
                for(JavaSqlType type : columnTypes)
                {
                    statement.setObject(i + 1, row[i], type);
                    i++;
                }

                statement.addBatch();
            }

            statement.executeBatch();
        }

        log.info("Rows inserted");
    }

    @Override
    public void clearTable(Connection connection, Table table) throws SQLException
    {
        log.fine("Clearing table " + table.getName());

        try(Statement statement = connection.createStatement())
        {
            //noinspection SqlWithoutWhere
            statement.executeUpdate("DELETE FROM " + table.getName());
        }
    }

    @Override
    public void disableConstraints(DataSource ds) throws SQLException, IOException
    {
        try(Connection connection = ds.getConnection())
        {
            disableConstraints(connection);
        }
    }

    @Override
    public void disableConstraints(Connection connection) throws SQLException, IOException
    {
        log.fine("Disabling database constraints");

        try(Statement statement = connection.createStatement())
        {
            statement.execute(QueryLoader.loadQuery("disable_constraints"));
        }

        log.fine("Database constraints disabled");
    }

    @Override
    public void enableConstraints(DataSource ds) throws SQLException, IOException
    {
        try(Connection connection = ds.getConnection())
        {
            enableConstraints(connection);
        }
    }

    @Override
    public void enableConstraints(Connection connection) throws SQLException, IOException
    {
        log.fine("Enabling database constraints");

        try(Statement statement = connection.createStatement())
        {
            statement.execute(QueryLoader.loadQuery("enable_constraints"));
        }

        log.fine("Database constraints enabled");
    }

    @Override
    public Connection createTransaction(DataSource ds) throws SQLException
    {
        Connection connection = ds.getConnection();
        connection.setAutoCommit(false);

        log.fine("Created a transaction");

        return connection;
    }

    @Override
    public void commitTransaction(Connection connection) throws SQLException
    {
        log.fine("Committing transaction");
        connection.commit();
    }

    @Override
    public void rollbackTransaction(Connection connection) throws SQLException
    {
        log.fine("Rolling the transaction back");
        connection.rollback();
    }
}
