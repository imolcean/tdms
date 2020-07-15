package de.tu_berlin.imolcean.tdm.api.plugins;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDto;
import org.pf4j.ExtensionPoint;

import javax.sql.DataSource;

// TODO JavaDoc
// TODO Throw SchemaUpdateException
public interface SchemaUpdater extends ExtensionPoint
{
    /**
     * Updates database schema using the provided {@link DataSource}.
     *
     * @param internalDs {@link DataSource} of the database whose schema is being updated
     * @throws Exception if something goes wrong while updating the schema
     */
    SchemaUpdateDto initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception;

    void commitSchemaUpdate(SchemaUpdateDto update);

    void cancelSchemaUpdate() throws Exception;

    boolean isUpdateInProgress();
}
