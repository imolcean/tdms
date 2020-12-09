package io.github.imolcean.tdms.x.generators;

import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.api.interfaces.generation.generator.SimpleDataGenerator;
import io.github.imolcean.tdms.api.services.DataService;
import io.github.imolcean.tdms.api.services.SchemaService;
import lombok.extern.java.Log;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.SQLException;

@Component
@Extension
@Log
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class ClearDataGenerator implements SimpleDataGenerator
{
    @Autowired
    SchemaService schemaService;

    @Autowired
    DataService dataService;

    @Override
    public void generate(DataSourceWrapper ds) throws SchemaCrawlerException, IOException, SQLException
    {
        log.info("Clearing the internal database");

        dataService.clearTables(ds, schemaService.getSchema(ds).getTables());
    }
}
