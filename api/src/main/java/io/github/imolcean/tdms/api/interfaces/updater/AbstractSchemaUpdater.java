package io.github.imolcean.tdms.api.interfaces.updater;

import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.api.services.LowLevelDataService;
import io.github.imolcean.tdms.api.services.SchemaService;
import io.github.imolcean.tdms.api.services.DataService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log
public abstract class AbstractSchemaUpdater implements SchemaUpdater
{
    @Autowired
    protected SchemaService schemaService;

    @Autowired
    protected DataService dataService;

    @Autowired
    protected LowLevelDataService lowLevelDataService;

    protected DataSourceWrapper internalDs;
    protected DataSourceWrapper tmpDs;

    @Override
    public boolean isUpdateInProgress()
    {
        return tmpDs != null && internalDs != null;
    }

    @Override
    public void commitSchemaUpdate() throws Exception
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Committing schema update");
        log.fine("Purging Internal DB");

        schemaService.purgeSchema(internalDs);

        log.fine("Copying schema and data from Temp DB into Internal DB");

        schemaService.copySchema(tmpDs, internalDs);
        dataService.copyData(tmpDs, internalDs, schemaService.getSchemaCompact(tmpDs).getTables());

        log.fine("Purging Temp DB");

        schemaService.purgeSchema(tmpDs);

        log.info("Schema update committed");

        this.tmpDs = null;
        this.internalDs = null;
    }

    @Override
    public void cancelSchemaUpdate() throws Exception
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Cancelling schema update");

        schemaService.purgeSchema(tmpDs);

        this.tmpDs = null;
        this.internalDs = null;

        log.info("Schema update cancelled");
    }
}
