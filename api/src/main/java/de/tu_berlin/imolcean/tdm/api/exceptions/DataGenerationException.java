package de.tu_berlin.imolcean.tdm.api.exceptions;

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
