package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.exceptions.*;
import de.tu_berlin.imolcean.tdm.core.entities.StageDataSourceParams;
import de.tu_berlin.imolcean.tdm.core.repositories.StageDataSourceParamsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DataSourceService
{
    private final DataSourceProxy internalDs;
    private final StageDataSourceParamsRepository stageDsParamsRepo;

    public DataSourceService(@Qualifier("InternalDataSource") DataSourceProxy internalDs,
                             StageDataSourceParamsRepository stageDsParamsRepo)
    {
        this.internalDs = internalDs;
        this.stageDsParamsRepo = stageDsParamsRepo;
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
        StageDataSourceParams params = stageDsParamsRepo.findByStageName(name)
                .orElseThrow(() -> new StageDataSourceNotFoundException(name));

        return new DataSourceProxy(params);
    }

    /**
     * Provides access to the {@link DataSourceProxy} objects of the
     * staging environments that are currently known.
     *
     * @return {@link Map} of all staging environments that are known at the moment with names as keys
     */
    public Map<String, DataSourceProxy> getAllStagesDataSources()
    {
        Map<String, DataSourceProxy> map = new HashMap<>();

        for(StageDataSourceParams params : stageDsParamsRepo.findAll())
        {
            map.put(params.getStageName(), new DataSourceProxy(params));
        }

        return map;
    }

    /**
     * Returns {@link DataSourceProxy} that is associated with the specified {@code name}.
     *
     * @param alias "internal" is an alias for the internal {@link DataSourceProxy},
     *              "current" is an alias for the {@link DataSourceProxy} of the currently selected stage
     * @throws InvalidDataSourceAliasException if the provided {@code alias} is invalid
     * @throws NoCurrentStageException if {@code alias} is "current" but there is no stage selected
     * @return {@link DataSourceProxy} associated with the {@code alias}
     */
    public DataSourceProxy getDataSourceByAlias(String alias)
    {
        switch(alias)
        {
            case "internal":
                return getInternalDataSource();
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
     * @return {@link DataSourceProxy} of the newly created stage
     */
    public DataSourceProxy storeStageDsParams(StageDataSourceParams params)
    {
        if(params.getStageName().equalsIgnoreCase("internal") || params.getStageName().equalsIgnoreCase("current"))
        {
            throw new InvalidStageNameException(params.getStageName());
        }

        if(stageDsParamsRepo.existsByStageName(params.getStageName()))
        {
            throw new StageDataSourceAlreadyExistsException(params.getStageName());
        }

        return new DataSourceProxy(stageDsParamsRepo.save(params));
    }

    /**
     * Updates parameters of an existing stage.
     *
     * @param params new {@link StageDataSourceParams} for the stage specified in {@code StageDataSourceParams::stageName}
     * @return new {@link DataSourceProxy} of the stage
     */
    public DataSourceProxy updateStageDataSource(StageDataSourceParams params)
    {
        StageDataSourceParams existing = stageDsParamsRepo.findByStageName(params.getStageName())
                .orElseThrow(() -> new StageDataSourceNotFoundException(params.getStageName()));

        params.setId(existing.getId());

        return new DataSourceProxy(stageDsParamsRepo.save(params));
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
