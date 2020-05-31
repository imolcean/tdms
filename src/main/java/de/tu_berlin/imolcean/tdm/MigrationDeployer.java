package de.tu_berlin.imolcean.tdm;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import de.danielbechler.util.Strings;
import de.tu_berlin.imolcean.tdm.utils.QueryLoader;
import lombok.extern.java.Log;
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
     */
    public boolean deploy() throws IOException, SQLException
    {
        log.info("Deploying test data into external DB");

        try(Connection internalConnection = internalDs.getConnection();
            Connection externalConnection = externalDs.getConnection())
        {
            log.fine("Looking for non-empty tables in TDM");

            Collection<String> tablesInternal = getNonEmptyTables(internalConnection);

            log.fine(String.format("Found %s non-empty tables", tablesInternal.size()));

            log.fine("Looking for non-empty tables in the external DB");

            Collection<String> tablesExternal = getNonEmptyTables(externalConnection);

            log.fine(String.format("Found %s non-empty tables", tablesExternal.size()));

            if(tablesExternal.size() > 0)
            {
                log.warning("External DB is not empty, deployment aborted");

                return false;
            }

            for(String table : tablesInternal)
            {
                deployTable(internalConnection, externalConnection, table);
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
    private Collection<String> getNonEmptyTables(Connection connection) throws SQLException, IOException
    {
        Collection<String> tables = new TreeSet<>();

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

    // TODO Break into several methods
    private void deployTable(Connection internalConnection, Connection externalConnection, String table) throws SQLException
    {
        log.info("Deploying table " + table);

        try(Statement selectStatement = internalConnection.createStatement())
        {
            // Get full content of the table

            ResultSet rs = selectStatement.executeQuery("SELECT * FROM " + table);

            log.fine(String.format("Table has %s columns", rs.getMetaData().getColumnCount()));

            // Get column names and types

            List<String> columnNames = new ArrayList<>();
            List<Integer> columnTypes = new ArrayList<>();

            for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++)
            {
                columnNames.add(rs.getMetaData().getColumnName(i));
                columnTypes.add(rs.getMetaData().getColumnType(i));

                log.fine(String.format("Column %s of type %s (%s)",
                        rs.getMetaData().getColumnName(i),
                        rs.getMetaData().getColumnTypeName(i),
                        rs.getMetaData().getColumnClassName(i)));
            }

            // Execute an INSERT statement for each row from the ResultSet

            String insertSql = String
                    .format("INSERT INTO %s (%s) VALUES (%s)",
                            table,
                            Strings.join(",", columnNames),
                            getPlaceholders(rs.getMetaData().getColumnCount()));

            try(PreparedStatement insertStatement = externalConnection.prepareStatement(insertSql))
            {
                int rowCnt = 1;
                while(rs.next())
                {
                    log.fine("Copying row " + rowCnt);

                    insertStatement.clearParameters();

                    ArrayList<Object> values = new ArrayList<>();

                    for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++)
                    {
                        values.add(rs.getObject(i));
                    }
                        int i = 1;
                        for(int type : columnTypes)
                        {
                            insertStatement.setObject(i, values.get(i - 1), type);
                            i++;
                        }

//                    insertStatement.executeUpdate();

                    rowCnt++;
                }
            }

            log.info(String.format("Deployment of table %s finished", table));
        }
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
}
