package de.tu_berlin.imolcean.tdm.core.deployment;

import lombok.extern.java.Log;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Log
@Deprecated
public class TableContentResultSetHandler implements ResultSetHandler<TableContent>
{
    @Override
    public TableContent handle(ResultSet rs) throws SQLException
    {
        // Get columns

        List<String> columnNames = new ArrayList<>();
        List<Integer> columnTypes = new ArrayList<>();

        log.fine(String.format("Table has %s columns", rs.getMetaData().getColumnCount()));

        for(int i = 0; i < rs.getMetaData().getColumnCount(); i++)
        {
            columnNames.add(rs.getMetaData().getColumnName(i + 1));
            columnTypes.add(rs.getMetaData().getColumnType(i + 1));

            log.fine(String.format("Column %s of type %s (%s)",
                    rs.getMetaData().getColumnName(i + 1),
                    rs.getMetaData().getColumnTypeName(i + 1),
                    rs.getMetaData().getColumnClassName(i + 1)));
        }

        // Get rows

        Queue<Object[]> rows = new LinkedList<>();

        while(rs.next())
        {
            Object[] row = new Object[rs.getMetaData().getColumnCount()];

            for(int i = 0; i < rs.getMetaData().getColumnCount(); i++)
            {
                row[i] = rs.getObject(i + 1);
            }

            rows.add(row);
        }

        return new TableContent(rows, columnNames, columnTypes);
    }
}
