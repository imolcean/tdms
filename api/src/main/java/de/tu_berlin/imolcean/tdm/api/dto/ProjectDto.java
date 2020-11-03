package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto
{
    private String projectName;

    private DataSourceDto internal;
    private DataSourceDto tmp;

    private GitRepositoryDto gitRepository;

    private String schemaUpdater;
    private String dataImporter;
    private String dataExporter;
    private String deployer;
    private String dataGenerator;

    private String dataDir;
}
