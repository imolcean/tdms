package de.tu_berlin.imolcean.tdm.core;

import com.google.common.collect.Table;
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
    public TableDataDto getTableData(DataSource ds, String tableName) throws SQLException
    {
        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement())
        {
            log.fine(String.format("Looking for table %s in DataSource %s", tableName, connection.getMetaData().getURL()));

            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);

            return new TableDataResultSetHandler().handle(rs);
        }
    }
}
