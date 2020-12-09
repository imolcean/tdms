package io.github.imolcean.tdms.core;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableContentResultSetHandler implements ResultSetHandler<List<Object[]>>
{
    @Override
    public List<Object[]> handle(ResultSet rs) throws SQLException
    {
        List<Object[]> rows = new ArrayList<>();

        while(rs.next())
        {
            rows.add(handleRow(rs, rs.getMetaData().getColumnCount()));
        }

        return rows;
    }

    public Object[] handleRow(ResultSet rs, int columnCount) throws SQLException
    {
        Object[] row = new Object[columnCount];

        for(int i = 0; i < columnCount; i++)
        {
            row[i] = rs.getObject(i + 1);
        }

        return row;
    }
}
