package de.tu_berlin.imolcean.tdm.deployment;

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
     *
     * TODO Transactions, so no rows are added when constraint violation occurs
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
                    log.warning(String.format("Table %s could not be deployed because of constraint violation, putting it back to the queue", table));

                    internalTables.add(table);
                }
            }

            // TODO Remove
//            deployTable(internalConnection, externalConnection, "PERSON");
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

        log.fine("Rows to insert: " + content.getRows().size());

        Map<String, String> fk2pk = getFk2PkSelfDependencies(internalConnection, table);

        String insertSql = String
                .format("INSERT INTO %s (%s) VALUES (%s)",
                        table,
                        Strings.join(",", content.getColumnNames()),
                        getPlaceholders(content.getColumnNames().size()));

        Collection<Object[]> insertedRows = new ArrayList<>();

        try(PreparedStatement insertStatement = externalConnection.prepareStatement(insertSql))
        {
            externalConnection.setAutoCommit(false);

            rowsCycle:
            while(content.getRows().peek() != null)
            {
                Object[] row = content.getRows().poll();
                assert row != null;

                // Checking row dependencies (only for self dependent tables)

                for(String fk : fk2pk.keySet())
                {
                    Object fkValue = row[content.getIndex(fk)];

                    boolean rowReferencesSelf = Objects.equals(row[content.getIndex(fk)], row[content.getIndex(fk2pk.get(fk))]);
                    boolean pkWithFkValueAlreadyInserted = insertedRows.stream()
                            .anyMatch(insertedRow -> insertedRow[content.getIndex(fk2pk.get(fk))].equals(fkValue));

                    if(fkValue != null && !pkWithFkValueAlreadyInserted && !rowReferencesSelf)
                    {
                        log.fine(String.format("Row with FK=%s needs a row that is not yet inserted, putting it back to the queue.", fkValue));

                        content.getRows().add(row);
                        continue rowsCycle;
                    }
                }

                // Copying the row

                insertStatement.clearParameters();

                int i = 0;
                for(int type : content.getColumnTypes())
                {
                    insertStatement.setObject(i + 1, row[i], type);
                    i++;
                }

                insertStatement.addBatch();
                insertedRows.add(row);
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
     * Looks for FK->PK dependencies inside the same table, i.e. dependency on PKs of other tables are ignored.
     *
     * @param connection {@link Connection} object of the database
     * @param table name of the table to query
     * @return {@link Map} of column names where the first element is the FK and the second one is the PK
     */
    private Map<String, String> getFk2PkSelfDependencies(Connection connection, String table) throws SQLException
    {
        Map<String, String> associations = new HashMap<>();
        ResultSet importedKeysRs = connection.getMetaData().getImportedKeys(connection.getCatalog(), connection.getSchema(), table);

        while(importedKeysRs.next())
        {
            String pkTable = importedKeysRs.getString("PKTABLE_NAME");
            if(pkTable.equals(table))
            {
                associations.put(importedKeysRs.getString("FKCOLUMN_NAME"), importedKeysRs.getString("PKCOLUMN_NAME"));

                log.fine(String.format("FK: %s -> %s", importedKeysRs.getString("FKCOLUMN_NAME"), importedKeysRs.getString("PKCOLUMN_NAME")));
            }
        }

        return associations;
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

    // TODO Remove
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
