package io.github.imolcean.tdms.core.services.managers;

import io.github.imolcean.tdms.api.interfaces.updater.SchemaUpdater;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class SchemaUpdateImplementationManager extends AbstractImplementationManager<SchemaUpdater>
{
    public SchemaUpdateImplementationManager(SpringPluginManager plugins)
    {
        super(plugins, SchemaUpdater.class);
    }

    @Override
    public void selectImplementation(String implClassName)
    {
        if(selected != null && selected.isUpdateInProgress())
        {
            throw new IllegalStateException("Schema update is currently in progress");
        }

        super.selectImplementation(implClassName);
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
