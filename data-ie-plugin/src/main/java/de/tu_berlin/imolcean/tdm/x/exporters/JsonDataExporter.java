package de.tu_berlin.imolcean.tdm.x.exporters;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.interfaces.exporter.DataExporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import org.pf4j.Extension;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

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
        List<Object[]> rows = tableContentService.getTableContent(ds, schemaService.getTable(ds, "address"));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(rows);

        System.out.println(json);
    }
}
