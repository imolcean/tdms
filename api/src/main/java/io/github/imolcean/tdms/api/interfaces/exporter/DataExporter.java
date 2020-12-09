package io.github.imolcean.tdms.api.interfaces.exporter;

import io.github.imolcean.tdms.api.interfaces.PublicInterface;
import org.pf4j.ExtensionPoint;

import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * Represents a piece of TDMS functionality that is responsible for exporting serialised content of a database,
 * usually the internal database.
 */
public interface DataExporter extends PublicInterface, ExtensionPoint
{
    /**
     * Exports data from the database to an external destination.
     *
     * Implementations of this interface should guarantee that
     * the data is either exported completely or not at all.
     *
     * Implementations of this interfce must not modify the database in any way. The caller
     * expects the database to be in the state that it was in before calling this method.
     *
     * @param ds the source for the exported data
     */
    void exportData(DataSource ds, Path exportDir) throws Exception;
}
