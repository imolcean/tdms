package io.github.imolcean.tdms.api.interfaces.importer;

import io.github.imolcean.tdms.api.interfaces.PublicInterface;
import org.pf4j.ExtensionPoint;

import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * Represents a piece of TDMS functionality that is responsible for importing serialised content into a database,
 * usually the internal database.
 */
public interface DataImporter extends PublicInterface, ExtensionPoint
{
    /**
     * Imports data from an external resource into the database.
     *
     * Implementations of this interface should use transactions to guarantee that
     * the data is either imported completely or not at all. In case an {@link Exception}
     * is thrown, the caller expects the database to be in the state that it was
     * in before calling this method.
     *
     * @param ds {@link DataSource} of the database that accepts imported data
     * @param importDir directory where the serialised data is located
     */
    void importData(DataSource ds, Path importDir) throws Exception;
}
