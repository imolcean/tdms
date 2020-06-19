package de.tu_berlin.imolcean.tdm.plugins.api;

import schemacrawler.schema.Table;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Collection;

public interface SchemaAwareImporter extends Importer
{
    /**
     * Imports data from file or directory ({@code path}) into the database
     * taking into account the database schema ({@code tables}).
     *
     * Implementations of this interface should use transactions to guarantee that
     * the data is either imported completely or not at all. In case an {@link Exception}
     * is thrown, the caller expects the database to be in the state that it was
     * in before the calling this method.
     *
     * @param path {@link Path} to the source of data (file or directory with files)
     * @param db {@link Connection} to the database that accepts imported data
     * @param tables {@link Collection} of {@link Table}s that the database has
     * @throws IOException if something goes wrong while reading the files
     */
    void importPath(Path path, Connection db, Collection<Table> tables) throws IOException;
}
