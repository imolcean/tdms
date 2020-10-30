package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.IllegalSizeOfTableContentRowException;
import de.tu_berlin.imolcean.tdm.api.exceptions.TableContentRowIndexOutOfBoundsException;
import de.tu_berlin.imolcean.tdm.api.services.DataService;
import de.tu_berlin.imolcean.tdm.api.services.LowLevelDataService;
import de.tu_berlin.imolcean.tdm.core.TableContentResultSetHandler;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log
public class DefaultDataService implements DataService
{
    // TODO DRY?
    // TODO Use TableContent instead of Maps and Arrays

    private final LowLevelDataService lowLevelDataService;

    public DefaultDataService(LowLevelDataService lowLevelDataService)
    {
        this.lowLevelDataService = lowLevelDataService;
    }

    @Override
    public int getTableRowCount(DataSource ds, Table table) throws SQLException
    {
        log.info("Retrieving row count of table " + table.getName());

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table.getName());

            rs.next();

            log.info("Row count retrieved");

            return rs.getInt(1);
        }
    }

    @Override
    public List<Object[]> getTableContent(DataSource ds, Table table) throws SQLException
    {
        try(Connection connection = ds.getConnection())
        {
            return lowLevelDataService.getTableContent(connection, table);
        }
    }

    @Override
    public List<Object> getTableContentForColumn(DataSource ds, Table table, Column column) throws SQLException
    {
        try(Connection connection = ds.getConnection())
        {
            return lowLevelDataService.getTableContentForColumn(connection, table, column);
        }
    }

    @Override
    public List<Object[]> getTableContentForColumns(DataSource ds, Table table, List<Column> columns) throws SQLException
    {
        try(Connection connection = ds.getConnection())
        {
            return lowLevelDataService.getTableContentForColumns(connection, table, columns);
        }
    }

    // FIXME: Insert multiple rows where one violates PK constraint, transaction rolled back, other rows are still written
    @Override
    public void insertRows(DataSource ds, Table table, List<Map<Column, Object>> rows) throws SQLException
    {
        List<Object[]> _rows = rows.stream()
                .map(row -> columnMap2Array(table, row))
                .collect(Collectors.toList());

        try(Connection connection = lowLevelDataService.createTransaction(ds))
        {
            try
            {
                lowLevelDataService.insertRows(ds.getConnection(), table, _rows);
            }
            catch(SQLException e)
            {
                log.warning("Insertion failed");
                lowLevelDataService.rollbackTransaction(connection);
                throw e;
            }

            lowLevelDataService.commitTransaction(connection);
        }
    }

    @Override
    public void importData(DataSource ds, Map<Table, List<Object[]>> data) throws SQLException, IOException
    {
        try(Connection connection = lowLevelDataService.createTransaction(ds))
        {
            lowLevelDataService.disableConstraints(connection);

            try
            {
                for(Map.Entry<Table, List<Object[]>> entry : data.entrySet())
                {
                    lowLevelDataService.insertRows(connection, entry.getKey(), entry.getValue());
                }
            }
            catch(SQLException e)
            {
                log.warning("Import failed");
                connection.rollback();
                throw e;
            }

            lowLevelDataService.enableConstraints(connection);
            lowLevelDataService.commitTransaction(connection);
        }
    }

    // TODO Return row
    @Override
    public void updateRow(DataSource ds, Table table, int rowIndex, Map<Column, Object> row) throws SQLException
    {
        log.info(String.format("Updating the row nr. %s in table %s", rowIndex, table.getName()));

        int rowCount = getTableRowCount(ds, table);
        if(rowCount <= rowIndex || rowIndex < 0)
        {
            throw new TableContentRowIndexOutOfBoundsException(table.getName(), rowCount, rowIndex);
        }

        Object[] presentRow = getTableContent(ds, table).get(rowIndex);
        Object[] _row = new Object[table.getColumns().size()];

        int i = 0;
        for(Column column : table.getColumns())
        {
            _row[i] = row.containsKey(column) ? row.get(column) : presentRow[i];
            i++;
        }

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            assert table.getColumns().size() == rs.getMetaData().getColumnCount();

            if(_row.length != table.getColumns().size())
            {
                throw new IllegalSizeOfTableContentRowException(table.getName(), table.getColumns().size(), _row.length);
            }

            int currentRowIndex = 0;
            while(rs.next())
            {
                if(currentRowIndex == rowIndex)
                {
                    for(int j = 0; j < table.getColumns().size(); j++)
                    {
                        rs.updateObject(j + 1, _row[j]);
                    }

                    rs.updateRow();
                    break;
                }

                currentRowIndex++;
            }
        }

        log.info("Row updated");
    }

    @Override
    public void deleteRow(DataSource ds, Table table, int rowIndex) throws SQLException
    {
        log.info(String.format("Deleting the row nr. %s from table %s", rowIndex, table.getName()));

        int rowCount = getTableRowCount(ds, table);

        if(rowCount <= rowIndex || rowIndex < 0)
        {
            throw new TableContentRowIndexOutOfBoundsException(table.getName(), rowCount, rowIndex);
        }

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            int currentRowIndex = 0;
            while(rs.next())
            {
                if(currentRowIndex == rowIndex)
                {
                    rs.deleteRow();
                    break;
                }

                currentRowIndex++;
            }
        }

        log.info("Row deleted");
    }

    @Override
    public void copyData(DataSource src, DataSource target, Collection<Table> tables) throws SQLException, IOException
    {
        log.info("Copying all data");

        try(Connection targetConnection = lowLevelDataService.createTransaction(target))
        {
            lowLevelDataService.disableConstraints(targetConnection);

            for(Table table : tables)
            {
                List<Object[]> data = getTableContent(src, table);

                try
                {
                    lowLevelDataService.insertRows(targetConnection, table, data);
                }
                catch(SQLException e)
                {
                    log.warning("Copying of data failed");
                    lowLevelDataService.rollbackTransaction(targetConnection);
                    throw e;
                }
            }

            lowLevelDataService.enableConstraints(targetConnection);
            lowLevelDataService.commitTransaction(targetConnection);
        }

        log.info("All data copied");
    }

    @Override
    public boolean isTableEmpty(DataSource ds, Table table) throws SQLException
    {
        return getTableRowCount(ds, table) == 0;
    }

    @Override
    public boolean areTablesEmpty(DataSource ds, Collection<Table> tables) throws SQLException
    {
        for(Table table : tables)
        {
            if(!isTableEmpty(ds, table))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void clearTable(DataSource ds, Table table) throws SQLException
    {
        log.info("Clearing table " + table.getName());

        try(Connection connection = lowLevelDataService.createTransaction(ds))
        {
            try
            {
                lowLevelDataService.clearTable(connection, table);
            }
            catch(SQLException e)
            {
                lowLevelDataService.rollbackTransaction(connection);
                throw e;
            }

            lowLevelDataService.commitTransaction(connection);
        }

        log.info("Table cleared");
    }

    @Override
    public void clearTables(DataSource ds, Collection<Table> tables) throws SQLException, IOException
    {
        log.info("Clearing all tables");

        try(Connection connection = lowLevelDataService.createTransaction(ds))
        {
            lowLevelDataService.disableConstraints(connection);

            try
            {
                for(Table table : tables)
                {
                    log.fine("Clearing table " + table.getName());

                    lowLevelDataService.clearTable(connection, table);
                }
            }
            catch(SQLException e)
            {
                log.warning("Clearing all tables failed");
                lowLevelDataService.rollbackTransaction(connection);
                throw e;
            }

            lowLevelDataService.enableConstraints(connection);
            lowLevelDataService.commitTransaction(connection);
        }

        log.info("All tables cleared");
    }

    private Object[] columnMap2Array(Table table, Map<Column, Object> row)
    {
        Object[] _row = new Object[table.getColumns().size()];

        int i = 0;
        for(Column column : table.getColumns())
        {
            _row[i] = row.get(column);
            i++;
        }

        return _row;
    }
}
