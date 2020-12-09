package io.github.imolcean.tdms.core.services.managers;

import io.github.imolcean.tdms.api.interfaces.importer.DataImporter;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class DataImportImplementationManager extends AbstractImplementationManager<DataImporter>
{
    public DataImportImplementationManager(SpringPluginManager plugins)
    {
        super(plugins, DataImporter.class);
    }
}
