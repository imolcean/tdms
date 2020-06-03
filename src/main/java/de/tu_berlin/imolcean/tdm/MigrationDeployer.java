package de.tu_berlin.imolcean.tdm;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import de.danielbechler.util.Strings;
import de.tu_berlin.imolcean.tdm.utils.QueryLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Performs migration-based deployment of the test data into an empty external database.
 *
 * Migration is the default deployment method for test data. It means that content of
 * every non-empty table in the TDMS will be copied into the external database.
 */
@Service
@Log
public class MigrationDeployer
{
    @Data
    @AllArgsConstructor
    private static class TableContent
    {
        private final String table;
        private final List<Object[]> rows;
        private final List<String> columnNames;
        private final List<Integer> columnTypes;
    }

    private final SQLServerDataSource internalDs;
    private final SQLServerDataSource externalDs;

    public MigrationDeployer(@Qualifier("InternalDataSource") SQLServerDataSource internalDs,
                             @Qualifier("ExternalDataSource") SQLServerDataSource externalDs)
    {
        this.internalDs = internalDs;
        this.externalDs = externalDs;
    }

    /**
     * Performs deployment of the test data stored in the internal database.
     * The content of all non-empty tables is copied into the external database which must be empty.
     * In case the external database contains any data, the deployment process will not be performed.
     *
     * @return {@code true} after the deployment is successfully finished, {@code false} otherwise
     *
     * TODO Transactions, so no rows are added when constraint violation occurs
     * TODO Batching INSERT requests
     */
    public boolean deploy() throws IOException, SQLException
    {
        log.info("Deploying test data into external DB");

        try(Connection internalConnection = internalDs.getConnection();
            Connection externalConnection = externalDs.getConnection())
        {
            log.fine("Looking for non-empty tables in TDM");

            Queue<String> internalTables = getNonEmptyTables(internalConnection);

            log.fine(String.format("Found %s non-empty tables", internalTables.size()));

            log.fine("Looking for non-empty tables in the external DB");

            int nonEmptyExternalTables = getNonEmptyTables(externalConnection).size();

            log.fine(String.format("Found %s non-empty tables", nonEmptyExternalTables));

            if(nonEmptyExternalTables > 0)
            {
                log.warning("External DB is not empty, deployment aborted");

                return false;
            }

            while(internalTables.size() > 0)
            {
                String table = internalTables.poll();

                try
                {
                    deployTable(internalConnection, externalConnection, table);
                }
                catch(SQLException e)
                {
                    log.fine("SQLState: " + e.getSQLState());

                    // Codes 23xxx mean constraint violation
                    if(Integer.parseInt(e.getSQLState()) / 1000 != 23)
                    {
                        throw e;
                    }

                    log.warning(e.getMessage());
                    log.warning(String.format("Table %s could not be deployed because of constraint violation, I will return to it later", table));

                    internalTables.add(table);
                }
            }

            // TODO Remove
//            deployTable(internalConnection, externalConnection, "GRUNDBUCHBLATTVORSYSTEM");
        }

        log.info("Deployment finished successfully");

        return true;
    }

    /**
     * Finds all non-empty tables in a database.
     *
     * @param connection JDBC {@link Connection} to a database
     * @return Names of non-empty database tables
     */
    private Queue<String> getNonEmptyTables(Connection connection) throws SQLException, IOException
    {
        Queue<String> tables = new LinkedList<>();

        try(Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery(QueryLoader.loadQuery("find_non_empty_tables"));

            while(rs.next())
            {
                tables.add(rs.getString("table_name"));
            }
        }

        return tables;
    }

    private void deployTable(Connection internalConnection, Connection externalConnection, String table) throws SQLException
    {
        log.info("Deploying table " + table);

        TableContent content = getTableContent(internalConnection, table);

        String insertSql = String
                .format("INSERT INTO %s (%s) VALUES (%s)",
                        table,
                        Strings.join(",", content.getColumnNames()),
                        getPlaceholders(content.getColumnNames().size()));

        try(PreparedStatement insertStatement = externalConnection.prepareStatement(insertSql))
        {
            int rowCnt = 1;
            for(Object[] row : content.getRows())
            {
                log.fine("Copying row " + rowCnt);

                insertStatement.clearParameters();

                int i = 1;
                for(int type : content.getColumnTypes())
                {
                    insertStatement.setObject(i, row[i - 1], type);
                    i++;
                }

                insertStatement.executeUpdate();

                rowCnt++;
            }
        }

        log.info("Finished deployment of table " + table);
    }

    /**
     * Retrieves all rows from the requested {@code table}.
     *
     * @param connection {@link Connection} object of the database
     * @param table name of the table to query
     * @return {@link TableContent} object containing rows of the table as well as columns' names and types
     */
    private TableContent getTableContent(Connection connection, String table) throws SQLException
    {
        List<Object[]> rows;
        List<String> columnNames = new ArrayList<>();
        List<Integer> columnTypes = new ArrayList<>();

        try(Statement selectStatement = connection.createStatement())
        {
            // Get full content of the table

            ResultSet rs = selectStatement.executeQuery("SELECT * FROM " + table);

            log.fine(String.format("Table has %s columns", rs.getMetaData().getColumnCount()));

            // Get column names and types

            for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++)
            {
                columnNames.add(rs.getMetaData().getColumnName(i));
                columnTypes.add(rs.getMetaData().getColumnType(i));

                log.fine(String.format("Column %s of type %s (%s)",
                        rs.getMetaData().getColumnName(i),
                        rs.getMetaData().getColumnTypeName(i),
                        rs.getMetaData().getColumnClassName(i)));
            }

            rows = new ArrayListHandler().handle(rs);
        }

        return new TableContent(table, rows, columnNames, columnTypes);
    }

    /**
     * Returns a {@code String} containing the given {@code number} of
     * placeholders for a JDBC {@link PreparedStatement}.
     *
     * @param number the amount of placeholders
     * @return {@code String} with the placeholders separated by commas
     */
    private String getPlaceholders(int number)
    {
        return new StringBuilder()
                .append("?,".repeat(number))
                .reverse()
                .deleteCharAt(0)
                .toString();
    }

//    private List<Object[]> rowDifference(List<Object[]> leftRows, List<Object[]> rightRows)
//    {
//        return leftRows.stream()
//                .filter(leftRow ->
//                {
//                    for(Object[] rightRow : rightRows)
//                    {
//                        if(rowsEqual(leftRow, rightRow))
//                        {
//                            return false;
//                        }
//                    }
//
//                    return true;
//                })
//                .collect(Collectors.toList());
//    }
//
//    private boolean rowsEqual(Object[] leftRow, Object[] rightRow)
//    {
//        assert leftRow.length == rightRow.length;
//
//        for(int i = 0; i < leftRow.length; i++)
//        {
//            if(!Objects.equals(leftRow[i], rightRow[i]))
//            {
//                return false;
//            }
//        }
//
//        return true;
//    }
}
