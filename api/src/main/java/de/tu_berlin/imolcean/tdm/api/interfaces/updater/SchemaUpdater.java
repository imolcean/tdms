package de.tu_berlin.imolcean.tdm.api.interfaces.updater;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.pf4j.ExtensionPoint;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.util.List;

/**
 * Represents a piece of TDMS functionality that is responsible for applying database schema changes,
 * usually with help of external tools (e.g. Liquibase, Flyway).
 *
 * Schema update is a process with several steps. An implementation of {@link SchemaUpdater}
 * applies new database schema to a Temp DB and provides a {@link SchemaUpdateReport} that describes all changes
 * that the new schema has with respect to the old one.
 *
 * Some often-used functionality is already implemented in {@link AbstractSchemaUpdater} and its subclasses, so it's
 * rarely necessary to implement all methods of this interface. If the implementation handles data mapping from the
 * old schema to the new one by itself, it has to extend {@link IterativeSchemaUpdater}.
 * In case it does not handle the data mapping but only provides a new schema, {@link SimpleSchemaUpdater} has to
 * be extended. The default implementation will then take care of the mapping. It will request SQL
 * mapping scripts for every changed table to be provided by the user. The tables that do not get a mapping
 * script will be mapped automatically if possible.
 */
public interface SchemaUpdater extends PublicInterface, ExtensionPoint
{
    @Data
    @AllArgsConstructor
    class SchemaUpdateReport
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

    @Deprecated
    void setDependencies(SchemaService schemaService, TableContentService tableContentService);

    /**
     * Indicates whether a schema update is in progress.
     *
     * @return {@code true} if a schema update has been started but not yet committed/cancelled,
     *         {@code false} otherwise
     */
    boolean isUpdateInProgress();

    /**
     * Applies new database schema to a Temp DB and provides a {@link SchemaUpdateReport}
     * that describes all changes in the new schema with respect to the old one.
     *
     * TODO Throw SchemaUpdateException
     *
     * @param internalDs internal storage of the test data with the old schema
     * @param tmpDs empty temporary storage that the new schema will be applied to
     * @return report of all changes in the new schema with respect to the old one
     */
    SchemaUpdateReport initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception;

    /**
     * Finishes the process of a schema update by copying updated schema and data from Temp DB into Internal DB.
     *
     * The commit process of {@link SimpleSchemaUpdater} includes an additional step before. It will map the data
     * from the old schema to the new one. Data from the untouched tables will be handled automatically. Newly added or
     * modified tables may require an SQL mapping script provided by the user as part of the {@code request}. If there
     * is no such script provided for an added/modified table, then an automatic mapping will be attempted.
     *
     * @param request describes how data from the old schema will be mapped to the new one
     */
    void commitSchemaUpdate(SchemaUpdateCommitRequest request) throws Exception;

    /**
     * Cancels the schema update process started previously.
     *
     * A call to this method must guarantee that the subsequent call to {@code isUpdateInProgress} returns
     * {@code false}. Apart from that, a call to this method should drop all tables from Temp DB and leave it
     * completely empty.
     */
    void cancelSchemaUpdate() throws Exception;
}
