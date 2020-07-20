package de.tu_berlin.imolcean.tdm.api.plugins;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.pf4j.ExtensionPoint;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.util.List;

// TODO JavaDoc
// TODO Throw SchemaUpdateException
public interface SchemaUpdater extends ExtensionPoint
{
    @Data
    @AllArgsConstructor
    class SchemaUpdate
    {
        @Data
        @AllArgsConstructor
        public static class Comparison
        {
            Table before;
            Table after;
        }

        List<String> untouchedTables;
        List<Table> addedTables;
        List<Table> deletedTables;
        List<Comparison> changedTables;
    }

    /**
     * Updates database schema using the provided {@link DataSource}.
     *
     * @param internalDs {@link DataSource} of the database whose schema is being updated
     * @throws Exception if something goes wrong while updating the schema
     */
    SchemaUpdate initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception;

    // TODO Take SchemaUpdateCommitRequest
    void commitSchemaUpdate(SchemaUpdateCommitRequest request) throws Exception;

    void cancelSchemaUpdate() throws Exception;

    boolean isUpdateInProgress();

    // TODO Remove
    void setSchemaService(SchemaService service);
}
