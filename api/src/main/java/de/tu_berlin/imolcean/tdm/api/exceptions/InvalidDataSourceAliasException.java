package de.tu_berlin.imolcean.tdm.api.exceptions;

public class InvalidDataSourceAliasException extends RuntimeException
{
    public InvalidDataSourceAliasException(String alias)
    {
        super(String.format("'%s' is not a valid alias for a DataSource", alias));
    }
}
