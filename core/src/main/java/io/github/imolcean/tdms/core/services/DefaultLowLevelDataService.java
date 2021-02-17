package io.github.imolcean.tdms.core.services;

import de.danielbechler.util.Strings;
import io.github.imolcean.tdms.api.services.LowLevelDataService;
import io.github.imolcean.tdms.core.TableContentResultSetHandler;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log
public class DefaultLowLevelDataService implements LowLevelDataService
{
    @Override
    public List<Object[]> getTableContent(Connection connection, String tableName) throws SQLException
    {
        log.info("Retrieving content of the table " + tableName);

        try(Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);

            log.info("Content retrieved");

            return new TableContentResultSetHandler().handle(rs);
        }
    }

    @Override
    public List<Object> getTableContentForColumn(Connection connection, Table table, Column column) throws SQLException
    {
        return getTableContentForColumns(connection, table, Collections.singletonList(column)).stream()
                .map(row -> row[0])
                .collect(Collectors.toList());
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
                    Object value = row[i];

                    if(type.getName().toLowerCase().contains("binary") && value != null)
                    {
                        log.fine("Converting value to a byte array");
                        value = Base64.getDecoder().decode((String) value);
                    }

                    statement.setObject(i + 1, value, type);
                    i++;
                }

                statement.addBatch();
            }

            statement.executeBatch();
        }

        log.info("Rows inserted");
    }

    @Override
    public void clearTable(Connection connection, String tableName) throws SQLException
    {
        log.fine("Clearing table " + tableName);

        try(Statement statement = connection.createStatement())
        {
            //noinspection SqlWithoutWhere
            statement.executeUpdate("DELETE FROM " + tableName);
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

        List<String> tableNames = null;
        try
        {
            tableNames = getTableNames(connection);
        }
        catch(SchemaCrawlerException e)
        {
            throw new IOException(e);
        }

        try(Statement statement = connection.createStatement())
        {
            for(String tableName : tableNames)
            {
                statement.addBatch("ALTER TABLE " + tableName + " NOCHECK CONSTRAINT all");
            }

            statement.executeBatch();
        }
        catch(Exception e)
        {
            log.warning("Database constraints cannot be disabled");
            throw e;
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

        List<String> tableNames = null;
        try
        {
            tableNames = getTableNames(connection);
        }
        catch(SchemaCrawlerException e)
        {
            throw new IOException(e);
        }

        try(Statement statement = connection.createStatement())
        {
            for(String tableName : tableNames)
            {
                statement.addBatch("ALTER TABLE " + tableName + " WITH CHECK CHECK CONSTRAINT all");
            }

            statement.executeBatch();
        }
        catch(Exception e)
        {
            log.warning("Database constraints cannot be enabled");
            throw e;
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

    private List<String> getTableNames(Connection connection) throws SQLException, SchemaCrawlerException
    {
        LoadOptions load = LoadOptionsBuilder.builder()
                .withInfoLevel(InfoLevel.minimum)
                .toOptions();

        SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                .withLimitOptions(getDefaultLimitOptions(getFullSchemaName(connection)))
                .withLoadOptions(load)
                .toOptions();

        Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

        return catalog.getTables().stream()
                .map(NamedObject::getName)
                .collect(Collectors.toList());
    }

    private String getFullSchemaName(Connection connection) throws SQLException
    {
        return String.format("%s.%s", connection.getCatalog(), connection.getSchema());
    }

    private LimitOptions getDefaultLimitOptions(String fullSchemaName)
    {
        return LimitOptionsBuilder.builder()
                .includeSchemas(new RegularExpressionInclusionRule(fullSchemaName))
                .includeTables(name -> !name.contains("sysdiagrams"))
                .tableTypes("TABLE")
                .toOptions();
    }
}
