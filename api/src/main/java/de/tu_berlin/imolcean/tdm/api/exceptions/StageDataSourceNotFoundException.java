package de.tu_berlin.imolcean.tdm.api.exceptions;

public class StageDataSourceNotFoundException extends RuntimeException
{
    public StageDataSourceNotFoundException(String stageName)
    {
        super("No DataSource configured for the stage named " + stageName);
    }
}
