package io.github.imolcean.tdms.core.repositories;

import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.api.dto.DataSourceDto;
import io.github.imolcean.tdms.api.exceptions.InvalidStageNameException;
import io.github.imolcean.tdms.api.exceptions.StageDataSourceNotFoundException;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("SpringDataMethodInconsistencyInspection")
@Repository
@Log
public class StageDataSourceRepository implements CrudRepository<DataSourceDto, String>
{
    private final List<String> RESERVED_IDS;

    @Value("${app.stages.path}")
    private String pluginsDir;
    private File storage;

    public StageDataSourceRepository()
    {
        RESERVED_IDS = Arrays.asList("internal", "tmp", "current");
    }

    @PostConstruct
    public void setUp()
    {
        storage = new File(pluginsDir);
        log.fine("StageDataSourceRepository initialised. Storage path is " + storage.getAbsolutePath());
    }

    @Override
    public <S extends DataSourceDto> S save(S entity)
    {
        throw new UnsupportedOperationException("Cannot save a data source without knowing the stage name");
    }

    @Override
    public <S extends DataSourceDto> Iterable<S> saveAll(Iterable<S> entities)
    {
        throw new UnsupportedOperationException("Cannot save a data source without knowing the stage name");
    }

    public DataSourceDto save(String s, DataSourceDto entity)
    {
        if(RESERVED_IDS.contains(s))
        {
            throw new InvalidStageNameException(s);
        }

        Properties properties = new Properties();
        properties.setProperty("driverClassName", entity.getDriverClassName());
        properties.setProperty("url", entity.getUrl());
        properties.setProperty("database", entity.getDatabase());
        properties.setProperty("username", entity.getUsername());
        properties.setProperty("password", entity.getPassword());

        deleteById(s);

        File file = new File(storage, s + ".properties");
        try
        {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        catch(IOException e)
        {
            log.warning("Cannot create the file " + file.getAbsolutePath());
            throw new RuntimeException(e);
        }

        try(OutputStream os = new FileOutputStream(file))
        {
            properties.store(os, null);
        }
        catch(IOException e)
        {
            log.warning("Cannot write to the file " + file.getAbsolutePath());
            throw new RuntimeException(e);
        }

        return new DataSourceDto(
                properties.getProperty("driverClassName"),
                properties.getProperty("url"),
                properties.getProperty("database"),
                properties.getProperty("username"),
                properties.getProperty("password"));
    }

    @Override
    public Optional<DataSourceDto> findById(String s)
    {
        Optional<File> _file = findFileById(s);

        if(_file.isEmpty())
        {
            return Optional.empty();
        }

        Properties properties = readProperties(_file.get());

        if(!validateProperties(properties))
        {
            throw new IllegalArgumentException(String.format("Data source '%s' properties are malformed", s));
        }

        return Optional.of(
                new DataSourceDto(
                        properties.getProperty("driverClassName"),
                        properties.getProperty("url"),
                        properties.getProperty("database"),
                        properties.getProperty("username"),
                        properties.getProperty("password")));
    }

    @Override
    public boolean existsById(String s)
    {
        return findById(s).isPresent();
    }

    @Override
    public Iterable<DataSourceDto> findAll()
    {
        List<DataSourceDto> all = new ArrayList<>();

        for(File file : propertyFilesInStorage())
        {
            Properties properties = readProperties(file);

            if(!validateProperties(properties))
            {
                throw new IllegalArgumentException(String.format("Data source '%s' properties are malformed", trimPropertiesExtension(file.getName())));
            }

            all.add(
                    new DataSourceDto(
                            properties.getProperty("driverClassName"),
                            properties.getProperty("url"),
                            properties.getProperty("database"),
                            properties.getProperty("username"),
                            properties.getProperty("password")));
        }

        return all;
    }

    @Override
    public Iterable<DataSourceDto> findAllById(Iterable<String> strings)
    {
        List<DataSourceDto> found = new ArrayList<>();

        for(String s : strings)
        {
            Optional<DataSourceDto> _ds = findById(s);

            if(_ds.isPresent())
            {
                found.add(_ds.get());
            }
            else
            {
                log.warning(String.format("Data source for stage '%s' not found", s));
            }
        }

        return found;
    }

    /**
     * Provides access to the {@link DataSourceWrapper} objects of the
     * staging environments that are currently known.
     *
     * @return {@link Map} of all staging environments that are known at the moment with names as keys
     */
    public Map<String, DataSourceDto> findAllAsMap()
    {
        Map<String, DataSourceDto> map = new HashMap<>();

        for(String stageName : findAllIds())
        {
            map.put(
                    stageName,
                    findById(stageName)
                            .orElseThrow(() -> new StageDataSourceNotFoundException(stageName)));
        }

        return map;
    }

    public Iterable<String> findAllIds()
    {
        return propertyFilesInStorage().stream()
                .map(file -> trimPropertiesExtension(file.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public long count()
    {
        return propertyFilesInStorage().size();
    }

    @Override
    public void deleteById(String s)
    {
        Optional<File> _file = findFileById(s);

        if(_file.isEmpty())
        {
            return;
        }

        try
        {
            Files.delete(_file.get().toPath());
        }
        catch(IOException e)
        {
            log.warning("Cannot delete file " + _file.get().getAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(DataSourceDto entity)
    {
        throw new UnsupportedOperationException("Cannot delete a data source without knowing the stage name");
    }

    @Override
    public void deleteAll(Iterable<? extends DataSourceDto> entities)
    {
        throw new UnsupportedOperationException("Cannot delete a data source without knowing the stage name");
    }

    @Override
    public void deleteAll()
    {
        for(File file : propertyFilesInStorage())
        {
            try
            {
                Files.delete(file.toPath());
            }
            catch(IOException e)
            {
                log.warning("Cannot delete file " + file.getAbsolutePath());
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    private Optional<File> findFileById(String s)
    {
        for(File file : propertyFilesInStorage())
        {
            if(trimPropertiesExtension(file.getName()).equals(s))
            {
                return Optional.of(file);
            }
        }

        return Optional.empty();
    }

    private Collection<File> propertyFilesInStorage()
    {
        return FileUtils.listFiles(storage, new String[] {"properties"}, false);
    }

    private String trimPropertiesExtension(String fileName)
    {
        return fileName.split(".properties")[0];
    }

    private Properties readProperties(File file)
    {
        Properties properties = new Properties();

        try(InputStream is = new FileInputStream(file))
        {
            properties.load(is);
        }
        catch(IOException e)
        {
            log.warning("Cannot read the file " + file.getAbsolutePath());
            throw new RuntimeException(e);
        }

        return properties;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validateProperties(Properties properties)
    {
        return properties.containsKey("driverClassName") &&
                properties.containsKey("url") &&
                properties.containsKey("database") &&
                properties.containsKey("username") &&
                properties.containsKey("password");
    }
}
