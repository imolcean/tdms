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

    public void deploy() throws IOException, SQLException
    {
        log.info("Looking for non-empty tables in TDM");

        Collection<String> tablesInternal = getNonEmptyTables(internalDs);

        log.info(String.format("Found %s non-empty tables", tablesInternal.size()));

        log.info("Looking for non-empty tables in the external DB");

        Collection<String> tablesExternal = getNonEmptyTables(externalDs);

        log.info(String.format("Found %s non-empty tables", tablesExternal.size()));

        if(tablesExternal.size() > 0)
        {
            log.warning("External DB is not empty, deployment aborted");

            return;
        }

        // TODO
//        for(String table : tablesInternal)
//        {
//            copyTableContent(table);
//        }

        deployTable(tablesInternal.iterator().next());
    }

    private Collection<String> getNonEmptyTables(SQLServerDataSource ds) throws SQLException, IOException
    {
        Collection<String> tables = new TreeSet<>();

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery(QueryLoader.loadQuery("find_non_empty_tables"));

            while(rs.next())
            {
                tables.add(rs.getString("table_name"));
            }
        }

        return tables;
    }

    private void deployTable(String table) throws SQLException
    {
        log.info("Copying table " + table);

        try(Connection internalConnection = internalDs.getConnection();
            Connection externalConnection = externalDs.getConnection();
            Statement selectStatement = internalConnection.createStatement();
            Statement truncateStatement = externalConnection.createStatement())
        {
            // Get full content of the table

            ResultSet rs = selectStatement.executeQuery("SELECT * FROM " + table);

            log.info(String.format("Table has %s columns", rs.getMetaData().getColumnCount()));

            // Get column names and types

            Map<String, Integer> columns = new TreeMap<>();

            for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++)
            {
                columns.put(rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnType(i));
            }

            // Execute an INSERT statement for each row from the ResultSet

            String insertSql = String
                    .format("INSERT INTO %s (%s) VALUES (%s)",
                            table,
                            Strings.join(",", columns.keySet()),
                            getPlaceholders(rs.getMetaData().getColumnCount()));

            while(rs.next())
            {
                ArrayList<Object> values = new ArrayList<>();

                for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++)
                {
                    values.add(rs.getObject(i));
                }

                try(PreparedStatement insertStatement = externalConnection.prepareStatement(insertSql))
                {
                    int i = 1;
                    for(int type : columns.values())
                    {
                        // TODO Deal with the types
                        insertStatement.setObject(i, values.get(i - 1), i);
                        i++;
                    }

                    insertStatement.executeUpdate();
                }
            }

            log.info("Deployment finished");
        }
    }

    private String getPlaceholders(int number)
    {
        return new StringBuilder()
                .append("?,".repeat(number))
                .reverse()
                .deleteCharAt(0)
                .toString();
    }
}
