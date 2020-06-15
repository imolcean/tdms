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
public class StageDataSourceLoader
{
    private final Path configs;

    public StageDataSourceLoader(@Value("${app.datasource.stages.path}") String configDir)
    {
        this.configs = Path.of(configDir);
    }

    public Map<Object, Object> loadAll() throws IOException
    {
        Map<Object, Object> result = new HashMap<>();

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

        return result;
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
