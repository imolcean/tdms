package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.interfaces.exporter.DataExporter;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class DataExportImplementationManager extends AbstractImplementationManager<DataExporter>
{
    public DataExportImplementationManager(SpringPluginManager plugins)
    {
        super(plugins, DataExporter.class);
    }
}
