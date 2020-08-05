package de.tu_berlin.imolcean.tdm.core.services.proxies;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDataMappingRequest;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class SchemaUpdateProxy extends AbstractPublicInterfaceProxy<SchemaUpdater> implements SchemaUpdater
{
    public SchemaUpdateProxy(SchemaUpdateImplementationManager manager)
    {
        super(manager, SchemaUpdater.class);
    }

    @Override
    public boolean isUpdateInProgress()
    {
        return getImplementation().isUpdateInProgress();
    }

    @Override
    public SchemaUpdater.SchemaUpdateReport initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        return getImplementation().initSchemaUpdate(internalDs, tmpDs);
    }

    @Override
    public void mapData(SchemaUpdateDataMappingRequest request) throws Exception
    {
        getImplementation().mapData(request);
    }

    @Override
    public void rollbackDataMapping() throws Exception
    {
        getImplementation().rollbackDataMapping();
    }

    @Override
    public void commitSchemaUpdate() throws Exception
    {
        getImplementation().commitSchemaUpdate();
    }

    @Override
    public void cancelSchemaUpdate() throws Exception
    {
        getImplementation().cancelSchemaUpdate();
    }
}
