package io.github.imolcean.tdms.core.services;

import io.github.imolcean.tdms.api.dto.DataSourceDto;
import io.github.imolcean.tdms.api.exceptions.*;
import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.core.StageSelectionContextHolder;
import io.github.imolcean.tdms.core.repositories.StageDataSourceRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

/**
 * This service is used to retrieve and manipulate database connections.
 */
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

    /**
     * Provides access to the internal database of the current project.
     *
     * @return reference to the internal database
     * @throws NoDataSourceSelectedException if there is no internal datasource selected, this usually means no project is currently open
     */
    public DataSourceWrapper getInternalDataSource()
    {
        if(internalDs == null)
        {
            throw new NoDataSourceSelectedException();
        }

        return internalDs;
    }

    /**
     * Sets the given datasource as a reference to the internal database.
     *
     * @param ds reference to the internal database
     */
    public void setInternalDataSource(DataSourceWrapper ds)
    {
        this.internalDs = ds;

        log.fine("Internal DS set to " + ds.getUrl());
    }

    /**
     * Unselects currently selected internal database.
     */
    public void clearInternalDataSource()
    {
        this.internalDs = null;

        log.fine("Internal DS cleared");
    }

    /**
     * Provides access to the temp database of the current project.
     *
     * @return reference to the internal database
     * @throws NoDataSourceSelectedException if there is no temp datasource selected, this usually means no project is currently open
     */
    public DataSourceWrapper getTmpDataSource()
    {
        if(tmpDs == null)
        {
            throw new NoDataSourceSelectedException();
        }

        return tmpDs;
    }

    /**
     * Sets the given datasource as a reference to the temp database.
     *
     * @param ds reference to the temp database
     */
    public void setTmpDataSource(DataSourceWrapper ds)
    {
        this.tmpDs = ds;

        log.fine("Temp DS set to " + ds.getUrl());
    }

    /**
     * Unselects currently selected temp database.
     */
    public void clearTmpDataSource()
    {
        this.tmpDs = null;

        log.fine("Temp DS cleared");
    }

    /**
     * Provides access to the {@link DataSourceWrapper} of the staging environment
     * that is currently selected in the {@link StageSelectionContextHolder}.
     *
     * @throws StageDataSourceNotFoundException if no {@link DataSourceWrapper} is configured for the currently selected stage
     * @throws NoCurrentStageException if no stage is currently selected
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
     * @throws StageDataSourceNotFoundException if {@code alias} is "current" but no {@link DataSourceWrapper} is configured for the currently selected stage
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
