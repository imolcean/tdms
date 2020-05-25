package de.tu_berlin.imolcean.tdm;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;

@Service
@Log
public class SchemaExtractor
{
    public Collection<Table> extractDboTables(DataSource ds, String dbName) throws Exception
    {
        try(Connection connection = ds.getConnection())
        {
            log.info(String.format("Connection with %s established", dbName));

            SchemaCrawlerOptions options =
                    SchemaCrawlerOptionsBuilder.builder()
                            .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard())
                            .includeSchemas(name -> name.contains(dbName + ".dbo"))
                            .includeTables(name -> !name.contains("sysdiagrams"))
                            .toOptions();

            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

            return catalog.getTables();
        }
    }
}
