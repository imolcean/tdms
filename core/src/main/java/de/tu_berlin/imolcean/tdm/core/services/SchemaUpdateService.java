package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDataMappingRequest;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@Log
public class SchemaUpdateService implements SchemaUpdater
{
    private final SchemaUpdateImplementationManager schemaUpdateImplementationManager;

    public SchemaUpdateService(SchemaUpdateImplementationManager schemaUpdateImplementationManager)
    {
        this.schemaUpdateImplementationManager = schemaUpdateImplementationManager;
    }

    @Override
    public boolean isUpdateInProgress()
    {
        return getSchemaUpdater().isUpdateInProgress();
    }

    @Override
    public SchemaUpdater.SchemaUpdateReport initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        return getSchemaUpdater().initSchemaUpdate(internalDs, tmpDs);
    }

    @Override
    public void mapData(SchemaUpdateDataMappingRequest request) throws Exception
    {
        getSchemaUpdater().mapData(request);
    }

    @Override
    public void rollbackDataMapping() throws Exception
    {
        getSchemaUpdater().rollbackDataMapping();
    }

    @Override
    public void commitSchemaUpdate() throws Exception
    {
        getSchemaUpdater().commitSchemaUpdate();
    }

    @Override
    public void cancelSchemaUpdate() throws Exception
    {
        getSchemaUpdater().cancelSchemaUpdate();
    }

    private SchemaUpdater getSchemaUpdater()
    {
        return schemaUpdateImplementationManager
                .getSelectedImplementation()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);
    }
}
