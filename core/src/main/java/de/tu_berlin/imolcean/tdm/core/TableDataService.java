package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.dto.TableDataDto;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@Log
public class TableDataService
{
    private final SchemaService schemaService;

    public TableDataService(SchemaService schemaService)
    {
        this.schemaService = schemaService;
    }

    // TODO Throw Exception if tableName doesn't exist
    // TODO Check FK constraints (UPDATE, DELETE)

    public int getCount(DataSource ds, String tableName) throws SQLException
    {
        log.fine("Looking for row count of table " + tableName);

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + tableName);

            rs.next();

            return rs.getInt(1);
        }
    }

    public TableDataDto getTableData(DataSource ds, String tableName) throws SQLException
    {
        log.fine("Looking for table " + tableName);

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);

            return new TableDataResultSetHandler().handle(rs);
        }
    }

    public void insertRow(DataSource ds, String tableName, Object[] row) throws SQLException
    {
        log.fine("Inserting a new row into table " + tableName);

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            // TODO Insert (as-is or with auto generated keys)
        }
    }

    public void updateRow(DataSource ds, String tableName, int rowIndex, Object[] row) throws SQLException
    {
        log.fine(String.format("Updating the row nr. %s in table %s", rowIndex, tableName));

        int rowCount = getCount(ds, tableName);

        if(rowCount <= rowIndex || rowIndex < 0)
        {
            throw new IndexOutOfBoundsException(String.format("Row index %s is invalid. There are %s rows in the table %s.", rowIndex, rowCount, tableName));
        }

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);

            if(row.length != rs.getMetaData().getColumnCount())
            {
                throw new IllegalArgumentException(
                        String.format("Provided row has %s columns but there are %s columns in table %s",
                                row.length,
                                rs.getMetaData().getColumnCount(),
                                tableName));
            }

            int currentRowIndex = 0;
            while(rs.next())
            {
                if(currentRowIndex == rowIndex)
                {
                    for(int i = 0; i < rs.getMetaData().getColumnCount(); i++)
                    {
                        rs.updateObject(i + 1, row[i]);
                    }

                    rs.updateRow();
                    break;
                }

                currentRowIndex++;
            }
        }
    }

    public void deleteRow(DataSource ds, String tableName, int rowIndex) throws SQLException
    {
        log.fine(String.format("Deleting the row nr. %s from table %s", rowIndex, tableName));

        int rowCount = getCount(ds, tableName);

        if(rowCount <= rowIndex || rowIndex < 0)
        {
            throw new IndexOutOfBoundsException(String.format("Row index %s is invalid. There are %s rows in the table %s.", rowIndex, rowCount, tableName));
        }

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);

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
    }
}
