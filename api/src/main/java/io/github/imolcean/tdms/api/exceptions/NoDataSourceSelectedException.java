package io.github.imolcean.tdms.api.exceptions;

public class NoDataSourceSelectedException extends RuntimeException
{
    public NoDataSourceSelectedException()
    {
        super("No DataSource is currently selected");
    }
}
