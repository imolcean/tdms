package io.github.imolcean.tdms.core.services.proxies;

import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.api.dto.SchemaUpdateDataMappingRequest;
import io.github.imolcean.tdms.api.interfaces.updater.SchemaUpdater;
import io.github.imolcean.tdms.core.services.managers.SchemaUpdateImplementationManager;
import org.springframework.stereotype.Service;

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
    public SchemaUpdater.SchemaUpdateReport initSchemaUpdate(DataSourceWrapper internalDs, DataSourceWrapper tmpDs) throws Exception
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
