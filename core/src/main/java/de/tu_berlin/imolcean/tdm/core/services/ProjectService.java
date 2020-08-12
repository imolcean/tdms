package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoOpenProjectException;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoImplementationSelectedException;
import de.tu_berlin.imolcean.tdm.api.interfaces.exporter.DataExporter;
import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataExportImplementationManager;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataImportImplementationManager;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

@Service
@Log
public class ProjectService
{
    // TODO Rewrite to use ProjectDto and JSON serialisation

    private final DataSourceService dsService;
    private final SchemaService schemaService;
    private final GitService gitService;
    private final SchemaUpdateImplementationManager schemaUpdateManager;
    private final DataImportImplementationManager dataImportManager;
    private final DataExportImplementationManager dataExportManager;

    private String projectName;

    public ProjectService(DataSourceService dsService,
                          SchemaService schemaService,
                          TableContentService tableContentService,
                          GitService gitService,
                          SchemaUpdateImplementationManager schemaUpdateManager,
                          DataImportImplementationManager dataImportManager,
                          DataExportImplementationManager dataExportManager)
    {
        this.dsService = dsService;
        this.schemaService = schemaService;
        this.gitService = gitService;
        this.schemaUpdateManager = schemaUpdateManager;
        this.dataImportManager = dataImportManager;
        this.dataExportManager = dataExportManager;
        this.projectName = null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    public void open(@RequestBody Properties project) throws Exception
    {
        if(Strings.isBlank(project.getProperty("name")))
        {
            throw new IllegalArgumentException("Project file contains no project name");
        }

        projectName = project.getProperty("name");


        dsService.setInternalDataSource(extractDs(project, "internal"));
        dsService.setTmpDataSource(extractDs(project, "tmp"));


        String gitUrl = project.getProperty("git.url");
        String gitDir = project.getProperty("git.dir");
        String gitToken = project.getProperty("git.token");

        if(Strings.isBlank(gitUrl) || Strings.isBlank(gitDir) || Strings.isBlank(gitToken))
        {
            throw new IllegalArgumentException("Git repository is wrongly configured");
        }

        gitService.openRepository(gitUrl, Path.of(gitDir), gitToken);


        if(!Strings.isBlank(project.getProperty("schemaUpdater")))
        {
            schemaUpdateManager.selectImplementation(project.getProperty("schemaUpdater"));
        }

        if(!Strings.isBlank(project.getProperty("dataImporter")))
        {
            dataImportManager.selectImplementation(project.getProperty("dataImporter"));
        }

        if(!Strings.isBlank(project.getProperty("dataExporter")))
        {
            dataExportManager.selectImplementation(project.getProperty("dataExporter"));
        }


        // TODO
//        schemaService.purgeSchema(dsService.getInternalDataSource());
//        schemaService.purgeSchema(dsService.getTmpDataSource());
//
//        SchemaUpdater schemaUpdater = schemaUpdateManager.getSelectedImplementation()
//                .orElseThrow(() -> new NoImplementationSelectedException(SchemaUpdater.class));
//
//        schemaUpdater.initSchemaUpdate(dsService.getInternalDataSource(), dsService.getTmpDataSource());
//        schemaUpdater.commitSchemaUpdate();
//
//        dataImportManager.getSelectedImplementation()
//                .orElseThrow(() -> new NoImplementationSelectedException(DataImporter.class))
//                .importData(dsService.getInternalDataSource());
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

        // TODO Test
        project.setProperty("git.url", gitService.getUrl());
        project.setProperty("git.dir", gitService.getDir().toString());

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

        try
        {
            String dataImporter = dataImportManager.getSelectedImplementation()
                    .orElseThrow(() -> new NoImplementationSelectedException(DataImporter.class))
                    .getClass()
                    .getName();

            project.setProperty("dataImporter", dataImporter);
        }
        catch(NoImplementationSelectedException e)
        {
            project.setProperty("dataImporter", "");
        }

        try
        {
            String dataExporter = dataExportManager.getSelectedImplementation()
                    .orElseThrow(() -> new NoImplementationSelectedException(DataExporter.class))
                    .getClass()
                    .getName();

            project.setProperty("dataExporter", dataExporter);
        }
        catch(NoImplementationSelectedException e)
        {
            project.setProperty("dataExporter", "");
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

        // TODO Check with Strings::isBlank()
        if(driverClassName == null || url == null || username == null || password == null)
        {
            throw new IllegalArgumentException(String.format("Data source '%s' is wrongly configured", dsName));
        }

        return new DataSourceWrapper(driverClassName, url, username, password);
    }
}
