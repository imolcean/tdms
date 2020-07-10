package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.exceptions.SchemaUpdaterNotFoundException;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchemaUpdaterService
{
    SpringPluginManager plugins;
    SchemaUpdater selected;

    public SchemaUpdaterService(SpringPluginManager plugins)
    {
        this.plugins = plugins;
        this.selected = null;
    }

    /**
     * This method is used to retrieve {@link SchemaUpdater} implementation that is currently selected.
     *
     * @return currently selected implementation of the {@link SchemaUpdater} interface
     *         or an empty {@link Optional} if there is no implementation selected currently
     */
    public Optional<SchemaUpdater> getSelectedSchemaUpdater()
    {
        return Optional.ofNullable(selected);
    }

    /**
     * This method is used to retrieve all available implementations of the {@link SchemaUpdater} interface.
     *
     * @return all implementations of the {@link SchemaUpdater} interface that are currently available for selection
     */
    public List<SchemaUpdater> getAvailableSchemaUpdaters()
    {
        return plugins.getExtensions(SchemaUpdater.class);
    }

    /**
     * Selects one of the available {@link SchemaUpdater} implementations to be used for updating the schema.
     *
     * @param className fully qualified class name of the {@link SchemaUpdater} implementation class
     */
    public void selectSchemaUpdater(String className)
    {
        List<SchemaUpdater> updaters = plugins.getExtensions(SchemaUpdater.class);

        selected = updaters.stream()
                .filter(updater -> updater.getClass().getName().equalsIgnoreCase(className))
                .findFirst()
                .orElseThrow(() -> new SchemaUpdaterNotFoundException(className));
    }

    /**
     * Clears the selection that is made using {@code selectSchemaUpdater} method.
     */
    public void clearSelection()
    {
        selected = null;
    }
}
