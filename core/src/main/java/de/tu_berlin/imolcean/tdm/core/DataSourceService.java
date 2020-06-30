package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoCurrentStageException;
import de.tu_berlin.imolcean.tdm.api.exceptions.StageDataSourceAlreadyExistsException;
import de.tu_berlin.imolcean.tdm.api.exceptions.StageDataSourceNotFoundException;
import de.tu_berlin.imolcean.tdm.api.exceptions.InvalidStageNameException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;

@Service
public class DataSourceService
{
    private final DataSourceProxy internalDs;

    private final StageDataSourceRepository stageDsRepository;

    public DataSourceService(@Qualifier("InternalDataSource") DataSourceProxy internalDs,
                             StageDataSourceRepository stageDsRepository)
    {
        this.internalDs = internalDs;
        this.stageDsRepository = stageDsRepository;
    }

    /**
     * Provides access to the {@link DataSourceProxy} of the internal database.
     *
     * @return {@link DataSourceProxy} of the internal database
     */
    public DataSourceProxy getInternalDataSource()
    {
        return internalDs;
    }

    /**
     * Provides access to the {@link DataSourceProxy} of the staging environment
     * that is currently selected in the {@link StageContextHolder}.
     *
     * @throws StageDataSourceNotFoundException if no {@link DataSourceProxy} is configured for the given {@code name}
     * @return {@link DataSourceProxy} of the currently selected staging environment
     */
    public DataSourceProxy getCurrentStageDataSource()
    {
        String currentStage = StageContextHolder.getStageName();

        if(currentStage == null)
        {
            throw new NoCurrentStageException();
        }

        return getStageDataSourceByName(currentStage);
    }

    /**
     * Provides access to the {@link DataSourceProxy} of the stage with the given {@code name}.
     *
     * @param name name of the stage
     * @throws StageDataSourceNotFoundException if no {@link DataSourceProxy} is configured for the given {@code name}
     * @return {@link DataSourceProxy} of the stage with the given {@code name}
     */
    public DataSourceProxy getStageDataSourceByName(String name)
    {
        return stageDsRepository.findByName(name)
                .orElseThrow(() -> new StageDataSourceNotFoundException(name));
    }

    /**
     * Provides access to the {@link DataSourceProxy} objects of the
     * staging environments that are currently known.
     *
     * @return {@link Map} of all staging environments that are known at the moment with names as keys
     */
    public Map<String, DataSourceProxy> getAllStagesDataSources()
    {
        return stageDsRepository.findAll();
    }

    /**
     * Returns {@link DataSourceProxy} that is associated with the specified {@code name}.
     *
     * @param alias "internal" is an alias for the internal {@link DataSourceProxy},
     *              "current" is an alias for the {@link DataSourceProxy} of the currently selected stage,
     *              other values are directly mapped to the names of the stages
     * @throws StageDataSourceNotFoundException if there is no {@link DataSourceProxy} associated with a stage named {@code alias}
     * @return {@link DataSourceProxy} associated with the {@code alias}
     */
    public DataSourceProxy getDataSourceByAlias(String alias)
    {
        if(alias.equalsIgnoreCase("internal"))
        {
            return getInternalDataSource();
        }

        if(alias.equalsIgnoreCase("current"))
        {
            return getCurrentStageDataSource();
        }

        return getStageDataSourceByName(alias);
    }

    /**
     * Creates a {@link DataSourceProxy} for a new stage called {@code name}.
     *
     * @param name name of the new stage
     * @param ds {@link DataSourceProxy} of the newly created stage
     * @return {@link DataSourceProxy} of the newly created stage
     */
    public DataSourceProxy createStageDataSource(String name, DataSourceProxy ds)
    {
        if(name.equalsIgnoreCase("internal") || name.equalsIgnoreCase("current"))
        {
            throw new InvalidStageNameException(name);
        }

        try
        {
            return stageDsRepository.createStageDataSource(name, ds);
        }
        catch(FileAlreadyExistsException e)
        {
            throw new StageDataSourceAlreadyExistsException(name);
        }
    }

    /**
     * Changes {@link DataSourceProxy} for the stage with the name {@code name}.
     *
     * @param name name of the stage
     * @param ds new {@link DataSourceProxy} for the stage
     * @return new {@link DataSourceProxy} of the stage
     */
    public DataSourceProxy updateStageDataSource(String name, DataSourceProxy ds)
    {
        try
        {
            return stageDsRepository.updateStageDataSource(name, ds);
        }
        catch(FileNotFoundException e)
        {
            throw new StageDataSourceNotFoundException(name);
        }
    }

    /**
     * Removes {@link DataSourceProxy} for the stage with the specified {@code name}.
     *
     * @param name name of the stage
     */
    public void deleteStageDataSource(String name)
    {
        try
        {
            stageDsRepository.deleteStageDataSource(name);
        }
        catch(FileNotFoundException e)
        {
            throw new StageDataSourceNotFoundException(name);
        }
    }
}
