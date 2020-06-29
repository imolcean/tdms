package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.dto.TableDataDto;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableDataResultSetHandler implements ResultSetHandler<TableDataDto>
{
    @Override
    public TableDataDto handle(ResultSet rs) throws SQLException
    {
        List<Object[]> rows = new ArrayList<>();

        while(rs.next())
        {
            Object[] row = new Object[rs.getMetaData().getColumnCount()];

            for(int i = 0; i < rs.getMetaData().getColumnCount(); i++)
            {
                row[i] = rs.getObject(i + 1);
            }

            rows.add(row);
        }

        return new TableDataDto(rs.getMetaData().getTableName(1), rows);
    }
}
