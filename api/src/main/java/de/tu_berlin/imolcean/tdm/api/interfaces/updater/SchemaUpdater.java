package de.tu_berlin.imolcean.tdm.api.interfaces.updater;

import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
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
 * Schema update is process with several steps. An implementation of {@link SchemaUpdater}
 * applies new database schema to a Temp DB and provides a {@link SchemaUpdateReport} that describes all changes
 * that the new schema has with respect to the old one.
 *
 * If the implementation handles data mapping from the old schema to the new one by itself, it has to implement
 * {@link DataAwareSchemaUpdater} and not this interface. In case it does not handle the data mapping but only
 * provides the schema, then the system core will take care of the mapping. It will request SQL mapping scripts
 * for every changed table to be provided by the user. The tables that do not get a mapping script will be mapped
 * automatically if possible.
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

    @Deprecated
    void setSchemaService(SchemaService service);
}
