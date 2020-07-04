package de.tu_berlin.imolcean.tdm.api.exceptions;

public class TableContentRowIndexOutOfBoundsException extends IndexOutOfBoundsException
{
    public TableContentRowIndexOutOfBoundsException(String tableName, int rowCount, int rowIndex)
    {
        super(String.format("Row index %s is invalid. There are %s rows in the table %s.", rowIndex, rowCount, tableName));
    }
}
