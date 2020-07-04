package de.tu_berlin.imolcean.tdm.api.exceptions;

public class IllegalSizeOfTableContentRowException extends RuntimeException
{
    public IllegalSizeOfTableContentRowException(String tableName, int requiredSize, int actualSize)
    {
        super(String.format("Provided row has %s columns but there are %s columns in table %s", actualSize, requiredSize, tableName));
    }
}
