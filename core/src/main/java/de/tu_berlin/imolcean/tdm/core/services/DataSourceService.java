package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.*;
import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.core.StageSelectionContextHolder;
import de.tu_berlin.imolcean.tdm.core.repositories.StageDataSourceRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Service
@Log
public class DataSourceService
{
    private final StageDataSourceRepository stageDsRepo;

    private DataSourceWrapper internalDs;
    private DataSourceWrapper tmpDs;

    public DataSourceService(StageDataSourceRepository stageDsRepo)
    {
        this.stageDsRepo = stageDsRepo;
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
     * that is currently selected in the {@link StageSelectionContextHolder}.
     *
     * @throws StageDataSourceNotFoundException if no {@link DataSourceWrapper} is configured for the given {@code name}
     * @return {@link DataSourceWrapper} of the currently selected staging environment
     */
    public DataSourceWrapper getCurrentStageDataSource()
    {
        String currentStage = StageSelectionContextHolder.getStageName();

        if(currentStage == null)
        {
            throw new NoCurrentStageException();
        }

        DataSourceDto dto = stageDsRepo.findById(currentStage)
                .orElseThrow(() -> new StageDataSourceNotFoundException(currentStage));

        return new DataSourceWrapper(dto);
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
}
