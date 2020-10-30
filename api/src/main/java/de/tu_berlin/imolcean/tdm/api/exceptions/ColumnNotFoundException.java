package de.tu_berlin.imolcean.tdm.api.exceptions;

import schemacrawler.schema.Table;

public class ColumnNotFoundException extends RuntimeException
{
    public ColumnNotFoundException(Table table, String columnName)
    {
        super(String.format("Column %s not found in table %s", columnName, table.getName()));
    }
}
