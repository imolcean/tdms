package de.tu_berlin.imolcean.tdm.core;

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

    public DataSourceProxy getDataSourceByName(String name) throws Exception
    {
        if(name.equalsIgnoreCase("internal"))
        {
            return internalDs;
        }

        DataSourceProxy stageDs = stageDsManager.getStageDataSourceByName(name);

        if(stageDs == null)
        {
            throw new Exception("No DataSource found with name " + name);
        }

        return stageDs;
    }
}
