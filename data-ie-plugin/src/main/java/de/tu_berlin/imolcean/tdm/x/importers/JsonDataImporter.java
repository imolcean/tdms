package de.tu_berlin.imolcean.tdm.x.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.pf4j.Extension;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Extension
@Log
public class JsonDataImporter implements DataImporter
{
    private final Path importDir;

    private SchemaService schemaService;
    private TableContentService tableContentService;

    public JsonDataImporter(Properties properties)
    {
        this.importDir = Paths.get(properties.getProperty("import.path"));

        log.fine("Import directory: " + importDir.toString());
    }

    // TODO Insert SchemaService through DI
    @Override
    public void setDependencies(SchemaService schemaService, TableContentService tableContentService)
    {
        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    @Override
    public void importData(DataSource ds) throws Exception
    {
        Collection<Table> tables = schemaService.getSchema(ds).getTables();

        log.fine("Checking that the database is empty");

        if(!tableContentService.areTablesEmpty(ds, tables))
        {
            throw new Exception("Cannot import into non-empty database");
        }

        log.fine("Database is empty");
        log.info("Importing " + importDir.toString());

        ObjectMapper mapper = new ObjectMapper();
        Map<Table, List<Object[]>> map = new HashMap<>();

        Iterator<File> it = FileUtils.iterateFiles(importDir.toFile(), null, true);
        while(it.hasNext())
        {
            TableContentDto dto = mapper.readValue(it.next(), TableContentDto.class);

            map.put(schemaService.getTable(ds, dto.getTableName()), dto.getData());
        }

        tableContentService.importData(ds, map);

        log.info(String.format("Import finished. %s tables affected.", map.keySet().size()));
    }
}
