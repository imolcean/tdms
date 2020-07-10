package de.tu_berlin.imolcean.tdm.api.exceptions;

public class NoSchemaUpdaterSelectedException extends RuntimeException
{
    public NoSchemaUpdaterSelectedException()
    {
        super("There is no schema updater selected currently");
    }
}
