package de.tu_berlin.imolcean.tdm.core.services.proxies;

import de.tu_berlin.imolcean.tdm.api.interfaces.exporter.DataExporter;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataExportImplementationManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class DataExportProxy extends AbstractPublicInterfaceProxy<DataExporter> implements DataExporter
{
    public DataExportProxy(DataExportImplementationManager manager)
    {
        super(manager, DataExporter.class);
    }

    @Override
    public void exportData(DataSource ds) throws Exception
    {
        getImplementation().exportData(ds);
    }
}
