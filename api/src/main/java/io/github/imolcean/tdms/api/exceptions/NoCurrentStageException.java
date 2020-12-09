package io.github.imolcean.tdms.api.exceptions;

public class NoCurrentStageException extends RuntimeException
{
    public NoCurrentStageException()
    {
        super("There is no stage selected currently");
    }
}
