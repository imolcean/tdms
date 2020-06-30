package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

// TODO implement Repository

@Service
@Log
public class StageDataSourceRepository
{
    private final Path configs;

    private Map<String, DataSourceProxy> stageName2Ds;

    public StageDataSourceRepository(@Value("${app.datasource.stages.path}") String configDir) throws IOException
    {
        this.configs = Path.of(configDir);
        this.stageName2Ds = new HashMap<>();

        this.loadConfigs();
    }

    /**
     * Provides access to the {@link DataSourceProxy} objects of the
     * staging environments that are currently known.
     *
     * @return {@link Map} of all staging environments that are known at the moment with names as keys
     */
    public Map<String, DataSourceProxy> findAll()
    {
        return new HashMap<>(stageName2Ds);
    }

    /**
     * Provides access to the {@link DataSourceProxy} of the
     * staging environment with the given {@code stageName}.
     *
     * @return {@link Optional} with the {@link DataSourceProxy} of the staging environment
     * or an empty one if no {@link DataSourceProxy} is configured for the provided stage name
     */
    public Optional<DataSourceProxy> findByName(String stageName)
    {
        log.fine("Retrieving DataSource for stage " + stageName);

        DataSourceProxy ds = stageName2Ds.get(stageName);

        if(ds == null)
        {
            log.warning("No DataSource found for stage " + stageName);
        }

        return Optional.ofNullable(ds);
    }

    public DataSourceProxy createStageDataSource(String name, DataSourceProxy ds) throws FileAlreadyExistsException
    {
        // TODO Create file & reload
        return null;
    }

    public DataSourceProxy updateStageDataSource(String name, DataSourceProxy ds) throws FileNotFoundException
    {
        // TODO Overwrite file & reload
        return null;
    }

    public void deleteStageDataSource(String name) throws FileNotFoundException
    {
        // TODO Remove file & reload
    }

    /**
     * Loads all configuration files with DB connection parameters of staging environments.
     * Whenever a configuration file gets added, changed, or removed call this method to reload everything.
     */
    public void loadConfigs() throws IOException
    {
        Map<String, DataSourceProxy> result = new HashMap<>();

        try(Stream<Path> paths = Files.walk(configs))
        {
            paths.forEach(path ->
            {
                if(path.toFile().isDirectory())
                {
                    return;
                }

                DataSourceProxy ds = load(path);

                if(ds != null)
                {
                    String stageName = FilenameUtils.removeExtension(path.getFileName().toString());

                    result.put(stageName, ds);
                }
            });
        }

        stageName2Ds = result;
    }

    private DataSourceProxy load(Path path)
    {
        try(InputStream fs = new FileInputStream(path.toFile()))
        {
            Properties config = new Properties();
            config.load(fs);

            return new DataSourceProxy(
                    config.getProperty("driver-class-name"),
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
        }
        catch(IOException e)
        {
            log.severe(String.format("DataSource config %s could not be loaded. I will ignore it.", path.toString()));
            return null;
        }
    }
}
