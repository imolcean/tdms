package io.github.imolcean.tdms.api.exceptions;

public class TableNotFoundException extends RuntimeException
{
    public TableNotFoundException(String tableName)
    {
        super("No table found with the name " + tableName);
    }
}
