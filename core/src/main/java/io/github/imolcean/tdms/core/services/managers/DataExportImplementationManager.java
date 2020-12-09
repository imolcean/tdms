package io.github.imolcean.tdms.core.services.managers;

import io.github.imolcean.tdms.api.interfaces.exporter.DataExporter;
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
