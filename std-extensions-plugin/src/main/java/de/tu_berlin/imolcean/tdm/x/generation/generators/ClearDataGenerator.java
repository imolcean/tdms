package de.tu_berlin.imolcean.tdm.x.generation.generators;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator.DataGenerator;
import de.tu_berlin.imolcean.tdm.api.services.DataService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import lombok.extern.java.Log;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.SQLException;

@Component
@Extension
@Log

// TODO Test!

public class ClearDataGenerator implements DataGenerator
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
