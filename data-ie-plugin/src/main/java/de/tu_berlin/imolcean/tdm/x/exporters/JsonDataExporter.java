package de.tu_berlin.imolcean.tdm.x.exporters;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.interfaces.exporter.DataExporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.x.JsonTableContent;
import lombok.extern.java.Log;
import org.pf4j.Extension;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Extension
@Log
public class JsonDataExporter implements DataExporter
{
//    private final Path exportDir;

    private SchemaService schemaService;
    private TableContentService tableContentService;

//    public JsonDataExporter(Properties properties)
//    {
//        this.exportDir = Paths.get(properties.getProperty("export.path"));
//
//        log.fine("Export directory: " + exportDir.toString());
//    }

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
        ObjectMapper mapper = new ObjectMapper();

        for(String tableName : schemaService.getOccupiedTableNames(ds))
        {
            Table table = schemaService.getTable(ds, tableName);

            List<String> columnNames = table.getColumns().stream()
                    .map(NamedObject::getName)
                    .collect(Collectors.toList());
            List<Object[]> rows = tableContentService.getTableContent(ds, table);

            JsonTableContent t = new JsonTableContent(tableName, columnNames, rows);

            String json = mapper.writeValueAsString(t);
            System.out.println(json);
        }
    }
}
