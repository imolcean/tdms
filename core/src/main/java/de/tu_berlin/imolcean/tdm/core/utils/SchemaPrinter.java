package de.tu_berlin.imolcean.tdm.core.utils;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;

public class SchemaPrinter
{
    public static void printCompact(Catalog catalog)
    {
        print(catalog, false);
    }

    public static void print(Catalog catalog)
    {
        print(catalog, true);
    }

    private static void print(Catalog catalog, boolean printColumns)
    {
        for(Table table : catalog.getTables())
        {
            System.out.print("---> " + table);

            if(table instanceof View)
            {
                System.out.println(" (VIEW)");
            }
            else
            {
                System.out.println();
            }

            if(printColumns)
            {
                for(Column column : table.getColumns())
                {
                    System.out.println(
                            String.format("     ---> %s (%s) %s %s",
                                    column,
                                    column.getColumnDataType(),
                                    column.isPartOfPrimaryKey() ? "PK" : "",
                                    column.isPartOfForeignKey() ? "FK" : ""));
                }
            }
        }
    }
}
