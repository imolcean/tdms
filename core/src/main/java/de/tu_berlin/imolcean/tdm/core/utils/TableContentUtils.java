package de.tu_berlin.imolcean.tdm.core.utils;

import schemacrawler.schema.Table;

@Deprecated
public class TableContentUtils
{
    public static int getColumnIndex(Table table, String columnName)
    {
        for(int i = 0; i < table.getColumns().size(); i++)
        {
            if(columnName.equalsIgnoreCase(table.getColumns().get(i).getName()))
            {
                return i;
            }
        }

        return -1;
    }
}
