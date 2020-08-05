package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class DataImportImplementationManager extends AbstractImplementationManager<DataImporter>
{
    private final SchemaService schemaService;
    private final TableContentService tableContentService;

    public DataImportImplementationManager(SpringPluginManager plugins,
                                           SchemaService schemaService,
                                           TableContentService tableContentService)
    {
        super(plugins, DataImporter.class);

        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    @Override
    public void selectImplementation(String implClassName)
    {
        super.selectImplementation(implClassName);

        // TODO Replace through DI
        selected.setDependencies(schemaService, tableContentService);
    }
}
