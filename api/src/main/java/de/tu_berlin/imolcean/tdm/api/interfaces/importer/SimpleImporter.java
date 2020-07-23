package de.tu_berlin.imolcean.tdm.api.interfaces.importer;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public interface SimpleImporter extends Importer
{
    /**
     * Imports data from file or directory ({@code path}) into the database.
     *
     * Implementations of this interface should use transactions to guarantee that
     * the data is either imported completely or not at all. In case an {@link Exception}
     * is thrown, the caller expects the database to be in the state that it was
     * in before calling this method.
     *
     * @param path {@link Path} to the source of data (file or directory with files)
     * @param ds {@link DataSource} of the database that accepts imported data
     * @throws IOException if something goes wrong while reading the files
     * @throws SQLException if something goes wrong while writing to the database
     */
    void importPath(Path path, DataSource ds) throws IOException, SQLException;
}
