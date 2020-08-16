package de.tu_berlin.imolcean.tdm.x.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Component
@Extension
@Log
public class JsonDataImporter implements DataImporter
{
    @Autowired
    private SchemaService schemaService;

    @Autowired
    private TableContentService tableContentService;

    @Override
    public void importData(DataSource ds, Path importDir) throws Exception
    {
        log.info("Importing " + importDir.toString());

        ObjectMapper mapper = new ObjectMapper();
        Map<Table, List<Object[]>> map = new HashMap<>();

        Iterator<File> it = FileUtils.iterateFiles(importDir.toFile(), null, true);
        while(it.hasNext())
        {
            TableContentDto dto = mapper.readValue(it.next(), TableContentDto.class);
            Table table = schemaService.getTable(ds, dto.getTableName());

            if(!tableContentService.isTableEmpty(ds, table))
            {
                throw new IllegalStateException("Cannot import into non-empty table " + table.getName());
            }

            map.put(table, dto.getData());
        }

        tableContentService.importData(ds, map);

        log.info(String.format("Import finished. %s tables affected.", map.keySet().size()));
    }
}
