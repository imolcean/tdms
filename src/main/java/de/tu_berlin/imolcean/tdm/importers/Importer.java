package de.tu_berlin.imolcean.tdm.importers;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public interface Importer
{
    /**
     * Tries to import all files from the given directory.
     *
     * @param dir directory containing files that have to be imported
     */
    default void importDirectory(Path dir) throws IOException
    {
        try(Stream<Path> paths = Files.walk(dir))
        {
            paths.forEach(this::importFile);
        }
    }

    /**
     * Tries to import the given file. If the file cannot be imported
     * for some reason, it will be ignored.
     *
     * @param path file that has to be imported
     */
    void importFile(Path path);
}
