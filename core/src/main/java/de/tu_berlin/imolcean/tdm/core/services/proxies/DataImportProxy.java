package de.tu_berlin.imolcean.tdm.core.services.proxies;

import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataImportImplementationManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.nio.file.Path;

@Service
public class DataImportProxy extends AbstractPublicInterfaceProxy<DataImporter> implements DataImporter
{
    public DataImportProxy(DataImportImplementationManager manager)
    {
        super(manager, DataImporter.class);
    }

    @Override
    public void importData(DataSource ds, Path importDir) throws Exception
    {
        getImplementation().importData(ds, importDir);
    }
}
