package de.tu_berlin.imolcean.tdm.api.exceptions;

public class NoDataSourceSelectedException extends RuntimeException
{
    public NoDataSourceSelectedException()
    {
        super("No DataSource is currently selected");
    }
}
