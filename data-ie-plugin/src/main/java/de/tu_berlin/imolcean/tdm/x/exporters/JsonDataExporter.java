package de.tu_berlin.imolcean.tdm.x.exporters;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.exporter.DataExporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.pf4j.Extension;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Extension
@Log
public class JsonDataExporter implements DataExporter
{
    private final Path exportDir;

    private SchemaService schemaService;
    private TableContentService tableContentService;

    public JsonDataExporter(Properties properties) throws IOException
    {
        this.exportDir = Paths.get(properties.getProperty("export.path"));

        try
        {
            Files.createDirectory(this.exportDir);
        }
        catch(FileAlreadyExistsException e)
        {
            if(!Files.isDirectory(this.exportDir))
            {
                throw new IllegalArgumentException("Provided export dir exists and is not a directory");
            }
        }

        log.fine("Export directory: " + exportDir.toString());
    }

    // TODO Insert SchemaService through DI
    @Override
    public void setDependencies(SchemaService schemaService, TableContentService tableContentService)
    {
        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    @Override
    public void exportData(DataSource ds) throws Exception
    {
        log.info("Exporting data");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPrettyPrinter(
                new DefaultPrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

        FileUtils.cleanDirectory(this.exportDir.toFile());

        for(String tableName : schemaService.getOccupiedTableNames(ds))
        {
            log.fine("Exporting " + tableName);

            Table table = schemaService.getTable(ds, tableName);

            List<String> columnNames = table.getColumns().stream()
                    .map(NamedObject::getName)
                    .collect(Collectors.toList());
            List<Object[]> rows = tableContentService.getTableContent(ds, table);

            TableContentDto dto = new TableContentDto(tableName, columnNames, rows);

            Path file = Files.createFile(Paths.get(exportDir.toAbsolutePath().toString(), tableName + ".json"));

            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), dto);

            log.fine(String.format("Export of %s finished", tableName));
        }

        log.info("Data export finished");
    }
}
