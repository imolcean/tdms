package de.tu_berlin.imolcean.tdm;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.sql.Connection;

@Service
@Log
public class SchemaExtractor
{
    public Catalog extractDboTables(SQLServerDataSource ds) throws Exception
    {
        try(Connection connection = ds.getConnection())
        {
            log.info(String.format("Connection with %s:%d:%s established", ds.getServerName(), ds.getPortNumber(), ds.getDatabaseName()));

            SchemaCrawlerOptions options =
                    SchemaCrawlerOptionsBuilder.builder()
                            .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard())
                            .includeSchemas(name -> name.contains(ds.getDatabaseName() + ".dbo"))
                            .includeTables(name -> !name.contains("sysdiagrams"))
                            .tableTypes("TABLE")
                            .toOptions();

            return SchemaCrawlerUtility.getCatalog(connection, options);
        }
    }
}
