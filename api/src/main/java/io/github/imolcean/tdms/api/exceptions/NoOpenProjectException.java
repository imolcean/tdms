package io.github.imolcean.tdms.api.exceptions;

public class NoOpenProjectException extends RuntimeException
{
    public NoOpenProjectException()
    {
        super("No project is currently open");
    }
}
