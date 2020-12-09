package io.github.imolcean.tdms.api.exceptions;

public class InvalidDataSourceAliasException extends RuntimeException
{
    public InvalidDataSourceAliasException(String alias)
    {
        super(String.format("'%s' is not a valid alias for a DataSource", alias));
    }
}
