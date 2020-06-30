package de.tu_berlin.imolcean.tdm.api.exceptions;

public class InvalidStageNameException extends RuntimeException
{
    public InvalidStageNameException(String stageName)
    {
        super("'%s' is not a valid stage name");
    }
}
