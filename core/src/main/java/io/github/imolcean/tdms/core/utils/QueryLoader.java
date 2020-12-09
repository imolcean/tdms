package io.github.imolcean.tdms.core.utils;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Utility class for loading SQL queries from files.
 */
public class QueryLoader
{
    private final static ResourceLoader resourceLoader;

    static {
        resourceLoader = new DefaultResourceLoader();
    }

    /**
     * Loads an SQL query from file.
     *
     * @param name name of the file (without .sql extension) that is located in the {@code resources/sql} directory
     * @return {@code String} containing the SQL query
     * @throws IOException when no file with the given {@code name} exists or it can't be read
     */
    public static String loadQuery(String name) throws IOException
    {
        Resource resource = resourceLoader.getResource("classpath:sql/" + name + ".sql");

        try(Reader reader = new InputStreamReader(resource.getInputStream()))
        {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
