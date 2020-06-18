package de.tu_berlin.imolcean.tdm.core;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
@Log
public class SchemaExtractor
{
    public Catalog extractDboTables(DataSource ds) throws Exception
    {
        try(Connection connection = ds.getConnection())
        {
            String fullSchemaName = String.format("%s.%s", connection.getCatalog(), connection.getSchema());

            SchemaCrawlerOptions options =
                    SchemaCrawlerOptionsBuilder.builder()
                            .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard())
                            .includeSchemas(name -> name.contains(fullSchemaName))
                            .includeTables(name -> !name.contains("sysdiagrams"))
                            .tableTypes("TABLE")
                            .toOptions();

            return SchemaCrawlerUtility.getCatalog(connection, options);
        }
    }
}
