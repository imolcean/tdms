package de.tu_berlin.imolcean.tdm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.sql.*;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Value("${data.mssql.server}")
    private String serverName;

    @Value("${data.mssql.port}")
    private int portNumber;

    @Value("${data.mssql.db}")
    private String databaseName;

    @Value("${data.mssql.user}")
    private String username;

    @Value("${data.mssql.password}")
    private String password;

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        String url = String.format("jdbc:sqlserver://%s:%d;database=%s;user=%s;password=%s", serverName, portNumber, databaseName, username, password);

        System.out.println("Connection URL: " + url);

        try(Connection connection = DriverManager.getConnection(url))
        {
            System.out.println(String.format("Connection with %s:%d:%s established", this.serverName, this.portNumber, this.databaseName));

            SchemaCrawlerOptions options =
                    SchemaCrawlerOptionsBuilder.builder()
                            .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard())
                            .includeSchemas(name -> name.contains(databaseName + ".dbo"))
                            .toOptions();

            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

            for(Schema schema : catalog.getSchemas())
            {
                System.out.println(schema);

                for(Table table : catalog.getTables(schema))
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
    }
}
