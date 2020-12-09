package io.github.imolcean.tdms.core.services.proxies;

import io.github.imolcean.tdms.api.interfaces.exporter.DataExporter;
import io.github.imolcean.tdms.core.services.managers.DataExportImplementationManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.nio.file.Path;

@Service
public class DataExportProxy extends AbstractPublicInterfaceProxy<DataExporter> implements DataExporter
{
    public DataExportProxy(DataExportImplementationManager manager)
    {
        super(manager, DataExporter.class);
    }

    @Override
    public void exportData(DataSource ds, Path exportDir) throws Exception
    {
        getImplementation().exportData(ds, exportDir);
    }
}
