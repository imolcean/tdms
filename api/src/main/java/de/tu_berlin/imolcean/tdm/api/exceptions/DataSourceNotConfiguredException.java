package de.tu_berlin.imolcean.tdm.api.exceptions;

public class DataSourceNotConfiguredException extends RuntimeException
{
    public DataSourceNotConfiguredException(String dsName)
    {
        super("No DataSource configured with the name " + dsName);
    }
}
