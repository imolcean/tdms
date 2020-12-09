package io.github.imolcean.tdms.api.exceptions;

public class StageDataSourceNotFoundException extends RuntimeException
{
    public StageDataSourceNotFoundException(String stageName)
    {
        super("No DataSource configured for the stage named " + stageName);
    }
}
