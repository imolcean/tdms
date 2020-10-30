package de.tu_berlin.imolcean.tdm.api.exceptions;

public class GenerationMethodNotFoundException extends RuntimeException
{
    public GenerationMethodNotFoundException(String generationMethodName)
    {
        super("No generation method found with the name " + generationMethodName);
    }
}
