package de.tu_berlin.imolcean.tdm.api.exceptions;

public class TableNotFoundException extends RuntimeException
{
    public TableNotFoundException(String tableName)
    {
        super("No table found with the name " + tableName);
    }
}
