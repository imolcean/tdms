package io.github.imolcean.tdms.api.exceptions;

public class StageDataSourceAlreadyExistsException extends RuntimeException
{
    public StageDataSourceAlreadyExistsException(String stageName)
    {
        super("DataSource for the stage named " + stageName + " already exists");
    }
}
