package io.github.imolcean.tdms.api.exceptions;

public class InvalidStageNameException extends RuntimeException
{
    public InvalidStageNameException(String stageName)
    {
        super(String.format("'%s' is not a valid stage name", stageName));
    }
}
