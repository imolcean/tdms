package io.github.imolcean.tdms.api.exceptions;

public class DataGenerationException extends RuntimeException
{
    public DataGenerationException(String message)
    {
        super(message);
    }

    public DataGenerationException(Throwable cause)
    {
        super(cause);
    }
}
