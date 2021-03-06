package io.github.imolcean.tdms.x.exporters;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.imolcean.tdms.api.dto.TableContentDto;
import io.github.imolcean.tdms.api.interfaces.exporter.DataExporter;
import io.github.imolcean.tdms.api.services.SchemaService;
import io.github.imolcean.tdms.api.services.DataService;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Extension
@Log
public class JsonDataExporter implements DataExporter
{
    @Autowired
    private SchemaService schemaService;

    @Autowired
    private DataService dataService;

    @Override
    public void exportData(DataSource ds, Path exportDir) throws Exception
    {
        log.info("Exporting data");
        log.fine("Export dir: " + exportDir);

        if(Files.exists(exportDir) && !Files.isDirectory(exportDir))
        {
            throw new IllegalArgumentException("Provided export dir exists and is not a directory");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDefaultPrettyPrinter(
                new DefaultPrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

        FileUtils.cleanDirectory(exportDir.toFile());

        for(String tableName : schemaService.getOccupiedTableNames(ds))
        {
            log.fine("Exporting " + tableName);

            Table table = schemaService.getTable(ds, tableName);

            List<String> columnNames = table.getColumns().stream()
                    .map(NamedObject::getName)
                    .collect(Collectors.toList());
            List<Object[]> rows = dataService.getTableContent(ds, tableName);

            log.fine(String.format("There are %s rows", rows.size()));

            TableContentDto dto = new TableContentDto(tableName, columnNames, rows);

            Path file = Files.createFile(Paths.get(exportDir.toAbsolutePath().toString(), tableName + ".json"));

            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), dto);

            log.fine(String.format("Export of %s finished", tableName));
        }

        log.info("Data export finished");
    }
}
