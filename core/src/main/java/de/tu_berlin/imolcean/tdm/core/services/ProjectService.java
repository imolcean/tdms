package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoOpenProjectException;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoImplementationSelectedException;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataImportImplementationManager;
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
    private final DataSourceService dsService;
    private final SchemaUpdateImplementationManager schemaUpdateManager;
    private final DataImportImplementationManager dataImportManager;

    private String projectName;

    public ProjectService(DataSourceService dsService,
                          SchemaUpdateImplementationManager schemaUpdateManager,
                          DataImportImplementationManager dataImportManager)
    {
        this.dsService = dsService;
        this.schemaUpdateManager = schemaUpdateManager;
        this.dataImportManager = dataImportManager;
        this.projectName = null;
    }

    public boolean isProjectOpen()
    {
        return projectName != null;
    }

    public String getProjectName()
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        return projectName;
    }

    public void renameProject(String name)
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        if(Strings.isBlank(name))
        {
            throw new IllegalArgumentException("Project name cannot be empty");
        }

        projectName = name;
    }

    public void open(@RequestBody Properties project)
    {
        if(Strings.isBlank(project.getProperty("name")))
        {
            throw new IllegalArgumentException("Project file contains no project name");
        }

        projectName = project.getProperty("name");

        dsService.setInternalDataSource(extractDs(project, "internal"));
        dsService.setTmpDataSource(extractDs(project, "tmp"));

        if(!Strings.isBlank(project.getProperty("schemaUpdater")))
        {
            schemaUpdateManager.selectImplementation(project.getProperty("schemaUpdater"));
        }

        if(!Strings.isBlank(project.getProperty("dataImporter")))
        {
            dataImportManager.selectImplementation(project.getProperty("dataImporter"));
        }
    }

    public Properties save()
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        Properties project = new Properties();

        project.setProperty("name", projectName);

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
                    .orElseThrow(() -> new NoImplementationSelectedException(SchemaUpdater.class))
                    .getClass()
                    .getName();

            project.setProperty("schemaUpdater", schemaUpdater);
        }
        catch(NoImplementationSelectedException e)
        {
            project.setProperty("schemaUpdater", "");
        }

        // TODO Add DataImporter

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
