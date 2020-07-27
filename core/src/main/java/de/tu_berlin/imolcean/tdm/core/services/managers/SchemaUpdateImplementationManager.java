package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.exceptions.SchemaUpdaterNotFoundException;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.PublicInterfaceImplementationManager;
import lombok.extern.java.Log;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log
public class SchemaUpdateImplementationManager implements PublicInterfaceImplementationManager<SchemaUpdater>
{
    private final SpringPluginManager plugins;
    private final SchemaService schemaService;
    private final TableContentService tableContentService;

    private SchemaUpdater selected;

    public SchemaUpdateImplementationManager(SpringPluginManager plugins,
                                             SchemaService schemaService,
                                             TableContentService tableContentService)
    {
        this.plugins = plugins;
        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
        this.selected = null;
    }

    @Override
    public Optional<SchemaUpdater> getSelectedImplementation()
    {
        return Optional.ofNullable(selected);
    }

    @Override
    public List<SchemaUpdater> getAvailableImplementations()
    {
        return plugins.getExtensions(SchemaUpdater.class);
    }

    @Override
    public void selectImplementation(String className)
    {
        if(selected != null && selected.isUpdateInProgress())
        {
            throw new IllegalStateException("Schema update is currently in progress");
        }

        List<SchemaUpdater> updaters = plugins.getExtensions(SchemaUpdater.class);

        selected = updaters.stream()
                .filter(updater -> updater.getClass().getName().equalsIgnoreCase(className))
                .findFirst()
                .orElseThrow(() -> new SchemaUpdaterNotFoundException(className));

        // TODO Replace through DI
        selected.setDependencies(schemaService, tableContentService);

        log.fine("Selected SchemaUpdater changed to " + className);
    }

    @Override
    public void clearSelection()
    {
        if(selected.isUpdateInProgress())
        {
            throw new IllegalStateException("Schema update is currently in progress");
        }

        selected = null;

        log.fine("Selection of SchemaUpdater cleared");
    }
}
