package de.tu_berlin.imolcean.tdm.utils;

import org.springframework.stereotype.Service;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;

import java.util.Collection;

@Service
public class SchemaPrinter
{
    public static void print(Collection<Table> tables)
    {
        for(Table table : tables)
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
