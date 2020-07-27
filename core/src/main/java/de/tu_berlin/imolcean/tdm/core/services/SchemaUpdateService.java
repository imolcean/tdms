package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@Log
public class SchemaUpdateService
{
    // TODO JavaDoc

    private final SchemaUpdateImplementationManager schemaUpdateImplementationManager;

    public SchemaUpdateService(SchemaUpdateImplementationManager schemaUpdateImplementationManager)
    {
        this.schemaUpdateImplementationManager = schemaUpdateImplementationManager;
    }

    public boolean isUpdateInProgress()
    {
        return getSchemaUpdater().isUpdateInProgress();
    }

    public SchemaUpdater.SchemaUpdateReport initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        return getSchemaUpdater().initSchemaUpdate(internalDs, tmpDs);
    }

    public void commitSchemaUpdate(SchemaUpdateCommitRequest request) throws Exception
    {
        getSchemaUpdater().commitSchemaUpdate(request);
    }

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
