package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.exceptions.DataSourceNotConfiguredException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DataSourceService
{
    private final DataSourceProxy internalDs;

    private final StageDataSourceManager stageDsManager;

    public DataSourceService(@Qualifier("InternalDataSource") DataSourceProxy internalDs,
                             StageDataSourceManager stageDsManager)
    {
        this.internalDs = internalDs;
        this.stageDsManager = stageDsManager;
    }

    public DataSourceProxy getDataSourceByName(String name)
    {
        if(name.equalsIgnoreCase("internal"))
        {
            return internalDs;
        }

        if(name.equalsIgnoreCase("current"))
        {
            return stageDsManager.getCurrentStageDataSource();
        }

        DataSourceProxy stageDs = stageDsManager.getStageDataSourceByName(name);

        if(stageDs == null)
        {
            throw new DataSourceNotConfiguredException(name);
        }

        return stageDs;
    }
}
