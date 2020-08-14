package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.*;
import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.core.StageContextHolder;
import de.tu_berlin.imolcean.tdm.core.entities.StageDataSourceParams;
import de.tu_berlin.imolcean.tdm.core.repositories.StageDataSourceParamsRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Log
public class DataSourceService
{
    private final StageDataSourceParamsRepository stageDsParamsRepo;

    private DataSourceWrapper internalDs;
    private DataSourceWrapper tmpDs;

    public DataSourceService(StageDataSourceParamsRepository stageDsParamsRepo)
    {
        this.stageDsParamsRepo = stageDsParamsRepo;
        this.internalDs = null;
        this.tmpDs = null;
    }

    public DataSourceWrapper getInternalDataSource()
    {
        if(internalDs == null)
        {
            throw new NoDataSourceSelectedException();
        }

        return internalDs;
    }

    public void setInternalDataSource(DataSourceWrapper ds)
    {
        this.internalDs = ds;

        log.fine("Internal DS set to " + ds.getUrl());
    }

    public void clearInternalDataSource()
    {
        this.internalDs = null;

        log.fine("Internal DS cleared");
    }

    public DataSourceWrapper getTmpDataSource()
    {
        if(tmpDs == null)
        {
            throw new NoDataSourceSelectedException();
        }

        return tmpDs;
    }

    public void setTmpDataSource(DataSourceWrapper ds)
    {
        this.tmpDs = ds;

        log.fine("Temp DS set to " + ds.getUrl());
    }

    public void clearTmpDataSource()
    {
        this.tmpDs = null;

        log.fine("Temp DS cleared");
    }

    /**
     * Provides access to the {@link DataSourceWrapper} of the staging environment
     * that is currently selected in the {@link StageContextHolder}.
     *
     * @throws StageDataSourceNotFoundException if no {@link DataSourceWrapper} is configured for the given {@code name}
     * @return {@link DataSourceWrapper} of the currently selected staging environment
     */
    public DataSourceWrapper getCurrentStageDataSource()
    {
        String currentStage = StageContextHolder.getStageName();

        if(currentStage == null)
        {
            throw new NoCurrentStageException();
        }

        return getStageDataSourceByName(currentStage);
    }

    /**
     * Provides access to the {@link DataSourceWrapper} of the stage with the given {@code name}.
     *
     * @param name name of the stage
     * @throws StageDataSourceNotFoundException if no {@link DataSourceWrapper} is configured for the given {@code name}
     * @return {@link DataSourceWrapper} of the stage with the given {@code name}
     */
    public DataSourceWrapper getStageDataSourceByName(String name)
    {
        StageDataSourceParams params = stageDsParamsRepo.findByStageName(name)
                .orElseThrow(() -> new StageDataSourceNotFoundException(name));

        return new DataSourceWrapper(params.getDriverClassName(), params.getUrl(), params.getUsername(), params.getPassword());
    }

    /**
     * Provides access to the {@link DataSourceWrapper} objects of the
     * staging environments that are currently known.
     *
     * @return {@link Map} of all staging environments that are known at the moment with names as keys
     */
    public Map<String, DataSourceWrapper> getAllStagesDataSources()
    {
        Map<String, DataSourceWrapper> map = new HashMap<>();

        for(StageDataSourceParams params : stageDsParamsRepo.findAll())
        {
            map.put(params.getStageName(), new DataSourceWrapper(params.getDriverClassName(), params.getUrl(), params.getUsername(), params.getPassword()));
        }

        return map;
    }

    /**
     * Returns {@link DataSourceWrapper} that is associated with the specified {@code name}.
     *
     * @param alias "internal" is an alias for the internal {@link DataSourceWrapper},
     *              "tmp" is an alias for the Temp {@link DataSourceWrapper},
     *              "current" is an alias for the {@link DataSourceWrapper} of the currently selected stage
     * @throws InvalidDataSourceAliasException if the provided {@code alias} is invalid
     * @throws NoCurrentStageException if {@code alias} is "current" but there is no stage selected
     * @return {@link DataSourceWrapper} associated with the {@code alias}
     */
    public DataSourceWrapper getDataSourceByAlias(String alias)
    {
        switch(alias)
        {
            case "internal":
                return getInternalDataSource();
            case "tmp":
                return getTmpDataSource();
            case "current":
                return getCurrentStageDataSource();
            default:
                throw new InvalidDataSourceAliasException(alias);
        }
    }

    /**
     * Stores parameters of a new stage.
     *
     * @param params {@link StageDataSourceParams} of the newly created stage
     * @return {@link DataSourceWrapper} of the newly created stage
     */
    public DataSourceWrapper storeStageDsParams(StageDataSourceParams params)
    {
        if(params.getStageName().equalsIgnoreCase("internal")
                || params.getStageName().equalsIgnoreCase("tmp")
                || params.getStageName().equalsIgnoreCase("current"))
        {
            throw new InvalidStageNameException(params.getStageName());
        }

        if(stageDsParamsRepo.existsByStageName(params.getStageName()))
        {
            throw new StageDataSourceAlreadyExistsException(params.getStageName());
        }

        StageDataSourceParams savedParams = stageDsParamsRepo.save(params);

        return new DataSourceWrapper(savedParams.getDriverClassName(), savedParams.getUrl(), savedParams.getUsername(), savedParams.getPassword());
    }

    /**
     * Updates parameters of an existing stage.
     *
     * @param params new {@link StageDataSourceParams} for the stage specified in {@code StageDataSourceParams::stageName}
     * @return new {@link DataSourceWrapper} of the stage
     */
    public DataSourceWrapper updateStageDataSource(StageDataSourceParams params)
    {
        StageDataSourceParams existing = stageDsParamsRepo.findByStageName(params.getStageName())
                .orElseThrow(() -> new StageDataSourceNotFoundException(params.getStageName()));

        params.setId(existing.getId());

        StageDataSourceParams savedParams = stageDsParamsRepo.save(params);

        return new DataSourceWrapper(savedParams.getDriverClassName(), savedParams.getUrl(), savedParams.getUsername(), savedParams.getPassword());
    }

    /**
     * Removes data source parameters for the stage with the specified {@code name}.
     *
     * @param name name of the stage
     */
    public void deleteStageDataSource(String name)
    {
        StageDataSourceParams params = stageDsParamsRepo.findByStageName(name)
                .orElseThrow(() -> new StageDataSourceNotFoundException(name));

        stageDsParamsRepo.delete(params);
    }
}
