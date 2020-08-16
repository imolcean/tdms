package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.api.dto.ProjectDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoOpenProjectException;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.core.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.DataSourceMapper;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.GitRepositoryMapper;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataExportImplementationManager;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataImportImplementationManager;
import de.tu_berlin.imolcean.tdm.core.services.managers.DeploymentImplementationManager;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@Log
public class ProjectService
{
    private final DataSourceService dsService;
    private final SchemaService schemaService;
    private final GitService gitService;
    private final SchemaUpdateImplementationManager schemaUpdateManager;
    private final DataImportImplementationManager dataImportManager;
    private final DataExportImplementationManager dataExportManager;
    private final DeploymentImplementationManager deploymentManager;

    private String projectName;
    private Path dataDir;

    public ProjectService(DataSourceService dsService,
                          SchemaService schemaService,
                          GitService gitService,
                          SchemaUpdateImplementationManager schemaUpdateManager,
                          DataImportImplementationManager dataImportManager,
                          DataExportImplementationManager dataExportManager,
                          DeploymentImplementationManager deploymentManager)
    {
        this.dsService = dsService;
        this.schemaService = schemaService;
        this.gitService = gitService;
        this.schemaUpdateManager = schemaUpdateManager;
        this.dataImportManager = dataImportManager;
        this.dataExportManager = dataExportManager;
        this.deploymentManager = deploymentManager;

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

    public Path getDataDir()
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        return dataDir;
    }

    public void changeDataDir(Path dir)
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        dataDir = dir;
    }

    public void open(ProjectDto project) throws Exception
    {
        // TODO Close open project first

        log.info("Opening project");

        projectName = project.getProjectName();
        dataDir = Path.of(project.getDataDir());

        dsService.setInternalDataSource(createDsFromDto(project.getInternal()));
        dsService.setTmpDataSource(createDsFromDto(project.getTmp()));

        gitService.openRepository(
                project.getGitRepository().getUrl(),
                Path.of(project.getGitRepository().getDir()),
                project.getGitRepository().getToken());

        if(project.getSchemaUpdater() != null)
        {
            schemaUpdateManager.selectImplementation(project.getSchemaUpdater());
        }
        else
        {
            log.warning("SchemaUpdater not configured");
        }

        if(project.getDataImporter() != null)
        {
            dataImportManager.selectImplementation(project.getDataImporter());
        }
        else
        {
            log.warning("DataImporter not configured");
        }

        if(project.getDataExporter() != null)
        {
            dataExportManager.selectImplementation(project.getDataExporter());
        }
        else
        {
            log.warning("DataExporter not configured");
        }

        if(project.getDeployer() != null)
        {
            deploymentManager.selectImplementation(project.getDeployer());
        }
        else
        {
            log.warning("Deployer not configured");
        }

        // TODO Update schema, pull and import data

        log.info(String.format("Project %s opened successfully", projectName));
    }

    public ProjectDto save()
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        // TODO Export and push data

        return new ProjectDto(
                projectName,
                DataSourceMapper.toDto(dsService.getInternalDataSource()),
                DataSourceMapper.toDto(dsService.getTmpDataSource()),
                GitRepositoryMapper.toDto(gitService),
                schemaUpdateManager.getSelectedImplementation()
                        .map(impl -> impl.getClass().getName())
                        .orElse(null),
                dataImportManager.getSelectedImplementation()
                        .map(impl -> impl.getClass().getName())
                        .orElse(null),
                dataExportManager.getSelectedImplementation()
                        .map(impl -> impl.getClass().getName())
                        .orElse(null),
                deploymentManager.getSelectedImplementation()
                        .map(impl -> impl.getClass().getName())
                .orElse(null),
                dataDir.toString());
    }

    public void close()
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        log.info("Closing project " + projectName);

        projectName = null;
        dataDir = null;

        dsService.clearInternalDataSource();
        dsService.clearTmpDataSource();

        gitService.closeRepository();

        schemaUpdateManager.clearSelection();
        dataImportManager.clearSelection();
        dataExportManager.clearSelection();

        log.info("Project closed successfully");
    }

    private DataSourceWrapper createDsFromDto(DataSourceDto dto)
    {
        return new DataSourceWrapper(dto.getDriverClassName(), dto.getUrl(), dto.getUsername(), dto.getPassword());
    }
}
