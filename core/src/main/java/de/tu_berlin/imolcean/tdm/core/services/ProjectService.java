package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

@Service
@Log
public class ProjectService
{
    // TODO Create project by requesting params for internal DS and tmp DS

    private final DataSourceService dsService;
    private final SchemaUpdateImplementationManager schemaUpdateManager;

    public ProjectService(DataSourceService dsService, SchemaUpdateImplementationManager schemaUpdateManager)
    {
        this.dsService = dsService;
        this.schemaUpdateManager = schemaUpdateManager;
    }

    public void open(@RequestBody Properties project)
    {
        DataSourceWrapper internalDs = extractDs(project, "internal");
        DataSourceWrapper tmpDs = extractDs(project, "tmp");

        // TODO Set internal & tmp

        if(!Strings.isBlank(project.getProperty("schemaUpdater")))
        {
            schemaUpdateManager.selectImplementation(project.getProperty("schemaUpdater"));
        }
    }

    public Properties save()
    {
        Properties project = new Properties();

        DataSourceWrapper internalDs = dsService.getInternalDataSource();
        DataSourceWrapper tmpDs = dsService.getTmpDataSource();

        project.setProperty("internal.driverClassName", internalDs.getDriverClassName());
        project.setProperty("internal.url", internalDs.getUrl());
        project.setProperty("internal.username", internalDs.getUsername());
        project.setProperty("internal.password", internalDs.getPassword());

        project.setProperty("tmp.driverClassName", tmpDs.getDriverClassName());
        project.setProperty("tmp.url", tmpDs.getUrl());
        project.setProperty("tmp.username", tmpDs.getUsername());
        project.setProperty("tmp.password", tmpDs.getPassword());

        try
        {
            String schemaUpdater = schemaUpdateManager.getSelectedImplementation()
                    .orElseThrow(NoSchemaUpdaterSelectedException::new)
                    .getClass()
                    .getName();

            project.setProperty("schemaUpdater", schemaUpdater);
        }
        catch(NoSchemaUpdaterSelectedException e)
        {
            project.setProperty("schemaUpdater", "");
        }

        return project;
    }

    private DataSourceWrapper extractDs(Properties project, String dsName)
    {
        String driverClassName;
        String url;
        String username;
        String password;

        driverClassName = project.getProperty(dsName + ".driverClassName");
        url = project.getProperty(dsName + ".url");
        username = project.getProperty(dsName + ".username");
        password = project.getProperty(dsName + ".password");

        if(driverClassName == null || url == null || username == null || password == null)
        {
            throw new IllegalArgumentException(String.format("Data source '%s' is wrongly configured", dsName));
        }

        return new DataSourceWrapper(driverClassName, url, username, password);
    }
}
