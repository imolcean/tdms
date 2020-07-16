package de.tu_berlin.imolcean.tdm.core.deployment;

import de.danielbechler.util.Strings;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.utils.QueryLoader;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
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
    private final DataSourceService dsService;

    public MigrationDeployer(DataSourceService dsService)
    {
        this.dsService = dsService;
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
        DataSource externalDs = dsService.getCurrentStageDataSource();

        if(externalDs == null)
        {
            log.severe("Cannot get DataSource for the current stage. Deployment aborted.");
            return false;
        }

        log.info("Deploying test data into external DB");

        try(Connection internalConnection = dsService.getInternalDataSource().getConnection();
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
                log.severe("External DB is not empty, deployment aborted");

                return false;
            }

            log.fine("Disabling database constraints");
            try(Statement statement = externalConnection.createStatement())
            {
                statement.execute(QueryLoader.loadQuery("disable_constraints"));
            }

            while(internalTables.size() > 0)
            {
                deployTable(internalConnection, externalConnection, internalTables.poll());
            }

            log.fine("Enabling database constraints");
            try(Statement statement = externalConnection.createStatement())
            {
                statement.execute(QueryLoader.loadQuery("enable_constraints"));
            }
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

    /**
     * Performs migration of a single {@code table} from the internal TDMS database into the target external database.
     *
     * The table is being copied row by row. If any row fails to be copied, the whole transaction is being rolled back
     * and the table in the external database remains empty.
     *
     * @param internalConnection JDBC {@link Connection} to the internal database of the TDMS
     * @param externalConnection JDBC {@link Connection} to the target database of a staging environment
     * @param table name of the table to deploy
     */
    private void deployTable(Connection internalConnection, Connection externalConnection, String table) throws SQLException
    {
        log.info("Deploying table " + table);

        TableContent content = getTableContent(internalConnection, table);

        log.fine("Rows to insert: " + content.getRows().size());

        String insertSql = String
                .format("INSERT INTO %s (%s) VALUES (%s)",
                        table,
                        Strings.join(",", content.getColumnNames()),
                        getPlaceholders(content.getColumnNames().size()));

        try(PreparedStatement insertStatement = externalConnection.prepareStatement(insertSql))
        {
            externalConnection.setAutoCommit(false);

            while(content.getRows().peek() != null)
            {
                Object[] row = content.getRows().poll();
                assert row != null;

                insertStatement.clearParameters();

                int i = 0;
                for(int type : content.getColumnTypes())
                {
                    insertStatement.setObject(i + 1, row[i], type);
                    i++;
                }

                insertStatement.addBatch();
            }

            insertStatement.executeBatch();
            externalConnection.commit();

            externalConnection.setAutoCommit(true);
        }
        catch(SQLException e)
        {
            externalConnection.rollback();
            throw e;
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
        try(Statement selectStatement = connection.createStatement())
        {
            ResultSet rs = selectStatement.executeQuery("SELECT * FROM " + table);

            return new TableContentResultSetHandler().handle(rs);
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
