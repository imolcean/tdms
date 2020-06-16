package de.tu_berlin.imolcean.tdm;

import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Service
@Log
public class StageDataSourceManager
{
    private final Path configs;

    private Map<String, DataSource> stageName2Ds;

    public StageDataSourceManager(@Value("${app.datasource.stages.path}") String configDir) throws IOException
    {
        this.configs = Path.of(configDir);
        this.stageName2Ds = new HashMap<>();

        this.loadConfigs();
    }

    /**
     * Provides access to the {@link DataSource} of the staging environment that
     * is currently selected in the {@link StageContextHolder}.
     *
     * @return {@link DataSource} of the currently selected staging environment
     * @throws IllegalStateException if no {@link DataSource} could be found
     */
    public DataSource getCurrentStageDataSource() throws IllegalStateException
    {
        log.fine("Retrieving DataSource for stage " + StageContextHolder.getStageName());

        DataSource ds = stageName2Ds.get(StageContextHolder.getStageName());

        if(ds == null)
        {
            throw new IllegalStateException(String.format("No DataSource found for stage %s", StageContextHolder.getStageName()));
        }

        return ds;
    }

    /**
     * Loads all configuration files with DB connection parameters of staging environments.
     * Whenever a configuration file gets added, changed, or removed call this method to reload everything.
     */
    public void loadConfigs() throws IOException
    {
        Map<String, DataSource> result = new HashMap<>();

        try(Stream<Path> paths = Files.walk(configs))
        {
            paths.forEach(path ->
            {
                if(path.toFile().isDirectory())
                {
                    return;
                }

                DataSource ds = load(path);

                if(ds != null)
                {
                    String stageName = FilenameUtils.removeExtension(path.getFileName().toString());

                    result.put(stageName, ds);
                }
            });
        }

        stageName2Ds = result;
    }

    private DataSource load(Path path)
    {
        try(InputStream fs = new FileInputStream(path.toFile()))
        {
            Properties config = new Properties();
            config.load(fs);

            return DataSourceBuilder.create()
                    .driverClassName(config.getProperty("driver-class-name"))
                    .url(config.getProperty("url"))
                    .username(config.getProperty("username"))
                    .password(config.getProperty("password"))
                    .build();
        }
        catch(IOException e)
        {
            log.severe(String.format("DataSource config %s could not be loaded. I will ignore it.", path.toString()));
            return null;
        }
    }
}
