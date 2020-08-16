package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
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
