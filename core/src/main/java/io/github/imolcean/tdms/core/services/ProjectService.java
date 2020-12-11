package io.github.imolcean.tdms.core.services;

import io.github.imolcean.tdms.api.dto.ProjectDto;
import io.github.imolcean.tdms.api.exceptions.NoOpenProjectException;
import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.core.controllers.mappers.DataSourceMapper;
import io.github.imolcean.tdms.core.controllers.mappers.GitRepositoryMapper;
import io.github.imolcean.tdms.core.services.managers.*;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * This service is used to open, close, or modify TDMS projects.
 *
 * A project is a set of parameters ({@link ProjectDto}) that identify work environment. Usually,
 * a user would create one TDMS project for every software project which requires managing of the test data. In order
 * to use TDMS, one should create a new project or open an existing one.
 */
@Service
@Log
public class ProjectService
{
    private final DataSourceService dsService;
    private final GitService gitService;
    private final SchemaUpdateImplementationManager schemaUpdateManager;
    private final DataImportImplementationManager dataImportManager;
    private final DataExportImplementationManager dataExportManager;
    private final DeploymentImplementationManager deploymentManager;
    private final DataGenerationImplementationManager dataGenerationManager;

    private String projectName;
    private Path dataDir;

    public ProjectService(DataSourceService dsService,
                          GitService gitService,
                          SchemaUpdateImplementationManager schemaUpdateManager,
                          DataImportImplementationManager dataImportManager,
                          DataExportImplementationManager dataExportManager,
                          DeploymentImplementationManager deploymentManager,
                          DataGenerationImplementationManager dataGenerationManager)
    {
        this.dsService = dsService;
        this.gitService = gitService;
        this.schemaUpdateManager = schemaUpdateManager;
        this.dataImportManager = dataImportManager;
        this.dataExportManager = dataExportManager;
        this.deploymentManager = deploymentManager;
        this.dataGenerationManager = dataGenerationManager;

        this.projectName = null;
    }

    /**
     * Checks if there is any project open currently.
     *
     * @return true if a project is open, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isProjectOpen()
    {
        return projectName != null;
    }

    /**
     * Looks for the name of the currently open project.
     *
     * @return name of the currently open project
     * @throws NoOpenProjectException if there is no project open currently
     */
    public String getProjectName()
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        return projectName;
    }

    /**
     * Renames the currently open project.
     *
     * @param name new name for the currently open project
     * @throws NoOpenProjectException if there is no project open currently
     */
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

    /**
     * Finds the data directory of the currently open project.
     *
     * @return path to the data directory of the currently open project
     * @throws NoOpenProjectException if there is no project open currently
     */
    public Path getDataDir()
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        return dataDir;
    }

    /**
     * Changes the data directory of the currently open project.
     *
     * @param dir new data directory of the currently open project
     * @throws NoOpenProjectException if there is no project open currently
     */
    public void changeDataDir(Path dir)
    {
        if(!isProjectOpen())
        {
            throw new NoOpenProjectException();
        }

        dataDir = dir;
    }

    /**
     * Opens a project.
     *
     * @param project project to open
     */
    public void open(ProjectDto project) throws Exception
    {
        // TODO Close open project first

        log.info("Opening project");

        projectName = project.getProjectName();
        dataDir = Path.of(project.getDataDir());

        dsService.setInternalDataSource(new DataSourceWrapper(project.getInternal()));
        dsService.setTmpDataSource(new DataSourceWrapper(project.getTmp()));

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

        if(project.getDataGenerator() != null)
        {
            dataGenerationManager.selectImplementation(project.getDataGenerator());
        }
        else
        {
            log.warning("DataGenerator not configured");
        }

        // TODO Update schema, pull and import data

        log.info(String.format("Project %s opened successfully", projectName));
    }

    /**
     * Saves currently open project into a {@link ProjectDto} that can
     * be serialised, stored on disk, and reopened later.
     *
     * This method does not close the current project.
     *
     * @return serialised form of the currently open project
     */
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
                dataGenerationManager.getSelectedImplementation()
                        .map(impl -> impl.getClass().getName())
                        .orElse(null),
                dataDir.toString());
    }

    /**
     * Closes the currently open project.
     *
     * @throws NoOpenProjectException if there is no currently open project
     */
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
        deploymentManager.clearSelection();
        dataGenerationManager.clearSelection();

        log.info("Project closed successfully");
    }
}
