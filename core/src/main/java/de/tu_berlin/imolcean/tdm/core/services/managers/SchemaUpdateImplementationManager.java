package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.exceptions.SchemaUpdaterNotFoundException;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
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

    private SchemaUpdater selected;

    public SchemaUpdateImplementationManager(SpringPluginManager plugins, SchemaService schemaService)
    {
        this.plugins = plugins;
        this.schemaService = schemaService;
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
        List<SchemaUpdater> updaters = plugins.getExtensions(SchemaUpdater.class);

        selected = updaters.stream()
                .filter(updater -> updater.getClass().getName().equalsIgnoreCase(className))
                .findFirst()
                .orElseThrow(() -> new SchemaUpdaterNotFoundException(className));

        // TODO Replace through DI
        selected.setSchemaService(schemaService);

        log.fine("Selected SchemaUpdater changed to " + className);
    }

    @Override
    public void clearSelection()
    {
        selected = null;

        log.fine("Selection of SchemaUpdater cleared");
    }
}
