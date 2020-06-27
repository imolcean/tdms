package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
import de.tu_berlin.imolcean.tdm.core.StageDataSourceManager;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.DataSourceMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/datasource")
public class DataSourceController
{
    private final DataSourceProxy internalDs;

    private final StageDataSourceManager stageDsManager;

    public DataSourceController(@Qualifier("InternalDataSource") DataSourceProxy internalDs,
                                StageDataSourceManager stageDsManager)
    {
        this.internalDs = internalDs;
        this.stageDsManager = stageDsManager;
    }

    @GetMapping("/internal")
    public DataSourceDto getInternal()
    {
        return DataSourceMapper.toDto(internalDs);
    }

    @GetMapping("/stage/current")
    public DataSourceDto getCurrentStage()
    {
        return DataSourceMapper.toDto(stageDsManager.getCurrentStageDataSource());
    }

    @GetMapping("stages")
    public List<DataSourceDto> getAllStages()
    {
        return stageDsManager.getAllStagesDataSources().stream()
                .map(DataSourceMapper::toDto)
                .collect(Collectors.toList());
    }

    // TODO CrUD
}
