package de.tu_berlin.imolcean.tdm.api.exceptions;

public class NoOpenProjectException extends RuntimeException
{
    public NoOpenProjectException()
    {
        super("No project is currently open");
    }
}
