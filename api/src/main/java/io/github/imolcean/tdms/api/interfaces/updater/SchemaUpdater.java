package io.github.imolcean.tdms.api.interfaces.updater;

import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.api.dto.SchemaUpdateDataMappingRequest;
import io.github.imolcean.tdms.api.interfaces.PublicInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.pf4j.ExtensionPoint;
import schemacrawler.schema.Table;

import java.nio.file.Path;
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
 * In case it does not handle the data mapping but only provides a new schema, {@link DiffSchemaUpdater} has to
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

    /**
     * Makes this updater use specified description for update.
     *
     * Description can be a Liquibase changelog (use master changelog), Flyway migration script, etc.
     *
     * @param descriptor path to the update description
     */
    void setUpdateDescriptor(Path descriptor);

    /**
     * Indicates whether a schema update is in progress.
     *
     * @return {@code true} if a schema update has been started but not yet committed/cancelled,
     *         {@code false} otherwise
     */
    boolean isUpdateInProgress();

    /**
     * Indicates whether the data that needs migration scripts for mapping has already been mapped.
     *
     * @return {@code true} if data has been mapped,
     *         {@code false} if data has not yet been mapped or mapping is not required by the current updater
     */
    boolean isDataMapped();

    /**
     * Applies new database schema to a Temp DB and provides a {@link SchemaUpdateReport}
     * that describes all changes in the new schema with respect to the old one.
     *
     * @param internalDs internal storage of the test data with the old schema
     * @param tmpDs empty temporary storage that the new schema will be applied to
     * @return report of all changes in the new schema with respect to the old one
     */
    SchemaUpdateReport initSchemaUpdate(DataSourceWrapper internalDs, DataSourceWrapper tmpDs) throws Exception;

    /**
     * Maps all data from the old schema to the new schema.
     *
     * Data from the untouched tables will be handled automatically. Newly added or
     * is no such script provided for an added/modified table, then an automatic mapping will be attempted.
     * In case something goes wrong during the process, the whole mapping will be rolled back,
     * leaving the Temp DB in the state it was before.
     *
     * Note that descendants of {@link IterativeSchemaUpdater} perform data mapping as part of
     * schema update itself and therefore don't require this method to be called before commit.
     *
     * @param request describes how data from the old schema will be mapped to the new one
     */
    void mapData(SchemaUpdateDataMappingRequest request) throws Exception;

    /**
     * Removed data from Temp DB that were written there using {@code mapData} method.
     *
     * This method should be used in case Temp DB doesn't look as intended after data mapping was performed.
     * This method will erase all data from Temp DB and allow to perform mapping again.
     *
     * Note that descendants of {@link IterativeSchemaUpdater} perform data mapping as part of
     * schema update itself and therefore don't require this method to be called before commit.
     */
    void rollbackDataMapping() throws Exception;

    /**
     * Finishes the process of a schema update by copying updated schema and data from Temp DB into Internal DB.
     */
    void commitSchemaUpdate() throws Exception;

    /**
     * Cancels the schema update process started previously.
     *
     * A call to this method must guarantee that the subsequent call to {@code isUpdateInProgress} returns
     * {@code false}. Apart from that, a call to this method should drop all tables from Temp DB and leave it
     * completely empty.
     */
    void cancelSchemaUpdate() throws Exception;
}
