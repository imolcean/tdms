package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class SchemaUpdateImplementationManager extends AbstractImplementationManager<SchemaUpdater>
{
    private final SchemaService schemaService;
    private final TableContentService tableContentService;

    public SchemaUpdateImplementationManager(SpringPluginManager plugins,
                                             SchemaService schemaService,
                                             TableContentService tableContentService)
    {
        super(plugins, SchemaUpdater.class);

        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    @Override
    public void selectImplementation(String implClassName)
    {
        if(selected != null && selected.isUpdateInProgress())
        {
            throw new IllegalStateException("Schema update is currently in progress");
        }

        super.selectImplementation(implClassName);

        // TODO Replace through DI
        selected.setDependencies(schemaService, tableContentService);
    }

    @Override
    public void clearSelection()
    {
        if(selected != null && selected.isUpdateInProgress())
        {
            throw new IllegalStateException("Schema update is currently in progress");
        }

        super.clearSelection();
    }
}
