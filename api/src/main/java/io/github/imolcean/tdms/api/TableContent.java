package io.github.imolcean.tdms.api;

import lombok.Getter;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class TableContent
{
    @Getter
    public static class Row
    {
        private final Table table;
        private Object[] values;

        public Row(Table table)
        {
            this.table = table;
            this.values = new Object[table.getColumns().size()];
        }

        public Row(Table table, Object[] arr)
        {
            this(table);

            if(arr.length != table.getColumns().size())
            {
                throw new IllegalArgumentException(String.format("The row requires %s columns, %s provided", table.getColumns().size(), arr.length));
            }

            values = arr;
        }

        public Row(Table table, Map<Column, Object> map)
        {
            this(table);
            map.forEach((column, value) -> values[column.getOrdinalPosition() - 1] = value);
        }

        public Map<Column, Object> getValuesAsMap()
        {
            Map<Column, Object> map = new HashMap<>();

            for(Column column : table.getColumns())
            {
                map.put(column, getValue(column));
            }

            return map;
        }

        public Object getValue(Column column)
        {
            return values[column.getOrdinalPosition() - 1];
        }

        public void setValue(Column column, Object value)
        {
            values[column.getOrdinalPosition() - 1] = value;
        }

        public void clearValue(Column column)
        {
            setValue(column, null);
        }

        @Override
        public String toString()
        {
            return Arrays.stream(values)
                    .map(value -> value != null ? value.toString() : "null")
                    .collect(Collectors.joining(",\t"));
        }
    }

    private final Table table;
    private List<Row> rows;

    public static TableContent of(Table table, List<Map<Column, Object>> rows)
    {
        List<Row> _rows = rows.stream()
                .map(map -> new Row(table, map))
                .collect(Collectors.toList());

        return new TableContent(table, _rows);
    }

    public TableContent(Table table)
    {
        this.table = table;
        this.rows = new ArrayList<>();
    }

    public TableContent(Table table, List<Row> rows)
    {
        this(table);
        this.rows = rows;
    }

    public Row getRow(int row)
    {
        return rows.get(row);
    }

    public void addRow(Row row)
    {
        rows.add(row);
    }

    public List<Object[]> getRowsAsArrays()
    {
        return rows.stream()
                .map(Row::getValues)
                .collect(Collectors.toList());
    }

    public List<Map<Column, Object>> getRowsAsMaps()
    {
        return rows.stream()
                .map(Row::getValuesAsMap)
                .collect(Collectors.toList());
    }

    public void addRow(Map<Column, Object> row)
    {
        rows.add(new Row(table, row));
    }

    public void addRow(Object[] row)
    {
        rows.add(new Row(table, row));
    }

    public void deleteRow(int row)
    {
        rows.remove(row);
    }

    @Override
    public String toString()
    {
        String header = table.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.joining(",\t"));

        String rows = getRows().stream()
                .map(Row::toString)
                .collect(Collectors.joining("\n"));

        return String.format("%s\n%s", header, rows);
    }
}
