package de.tu_berlin.imolcean.tdm.deployment;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Data
class TableContent
{
    private final Queue<Object[]> rows;
    private final List<String> columnNames;
    private final List<Integer> columnTypes;
    private final Map<String, Integer> column2Index;

    public TableContent(Queue<Object[]> rows, List<String> columnNames, List<Integer> columnTypes)
    {
        if(columnNames.size() != columnTypes.size())
        {
            throw new IllegalArgumentException(
                    String.format("Collections' sizes don't match - columnNames: %s, columnTypes: %s",
                            columnNames.size(),
                            columnTypes.size()));
        }

        this.rows = rows;
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;

        this.column2Index = new HashMap<>();
        for(String column : this.columnNames)
        {
            this.column2Index.put(column, this.columnNames.indexOf(column));
        }
    }

    public int getIndex(String column)
    {
        return this.column2Index.get(column);
    }

    public Object getValue(int row, String column)
    {
        // TODO
        return null;
    }

    public Object getValue(Object[] row, String column)
    {
        // TODO
        return null;
    }

    public int getColumnType(String column)
    {
        // TODO
        return 0;
    }
}
