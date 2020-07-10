package de.tu_berlin.imolcean.tdm.api.plugins;

import org.pf4j.ExtensionPoint;

import javax.sql.DataSource;

// TODO JavaDoc
public interface SchemaUpdater extends ExtensionPoint
{
    /**
     * Updates database schema using the provided {@link DataSource}.
     *
     * @param ds {@link DataSource} of the database whose schema is being updated
     * @throws Exception if something goes wrong while updating the schema
     */
    void updateSchema(DataSource ds) throws Exception;
}
