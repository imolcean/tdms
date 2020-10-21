package de.tu_berlin.imolcean.tdm.api.interfaces.updater;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDataMappingRequest;
import lombok.extern.java.Log;

/**
 * Represents a special case of {@link SchemaUpdater} that migrates data
 * to the new schema by itself or using external tools.
 *
 * If a schema updater performs data migration (e.g. with DML section in Liquibase changesets),
 * then it should extend this class. When a schema update is performed using an {@link IterativeSchemaUpdater},
 * the system will not request mapping scripts from the user, neither will it try to map data automatically.
 */
@Log
public abstract class IterativeSchemaUpdater extends AbstractSchemaUpdater
{
    /**
     * Copies schema nad data from Internal DB to Temp DB.
     * Iterative schema update can then be made on the Temp DB using method {@code initSchemaUpdate}.
     */
    protected void prepare() throws Exception
    {
        log.info("Preparing Temp DB: copying schema and data");

        schemaService.copySchema(internalDs, tmpDs);
        dataService.copyData(internalDs, tmpDs, schemaService.getSchema(internalDs).getTables());

        log.info("Temp DB prepared");
    }

    @Override
    public void mapData(SchemaUpdateDataMappingRequest request)
    {
        throw new UnsupportedOperationException("Iterative schema updaters do not support data mapping as a separate step");
    }

    @Override
    public void rollbackDataMapping()
    {
        throw new UnsupportedOperationException("Iterative schema updaters do not support data mapping as a separate step");
    }
}
