package de.tu_berlin.imolcean.tdm.api.exceptions;

public class SchemaUpdaterNotFoundException extends RuntimeException
{
    public SchemaUpdaterNotFoundException(String name)
    {
        super("No schema updater found with the name " + name);
    }
}
