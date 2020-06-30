package de.tu_berlin.imolcean.tdm.api.exceptions;

public class NoCurrentStageException extends RuntimeException
{
    public NoCurrentStageException()
    {
        super("There is no stage selected currently");
    }
}
