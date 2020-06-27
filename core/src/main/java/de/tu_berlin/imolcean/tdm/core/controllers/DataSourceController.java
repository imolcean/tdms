package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
import de.tu_berlin.imolcean.tdm.core.StageContextHolder;
import de.tu_berlin.imolcean.tdm.core.StageDataSourceManager;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.DataSourceMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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

    // TODO crUD

    @GetMapping("/internal")
    public DataSourceDto getInternal()
    {
        return DataSourceMapper.toDto(internalDs);
    }

    @GetMapping("/stages")
    public Map<String, DataSourceDto> getAllStages()
    {
        return stageDsManager.getAllStagesDataSources().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> DataSourceMapper.toDto(entry.getValue())));
    }

    @PostMapping("/stages")
    public ResponseEntity<DataSourceDto> createStage(@RequestHeader("TDM-Stage-Name") String name,
                                                     @RequestBody DataSourceDto ds)
    {
        // TODO
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stage")
    public ResponseEntity<DataSourceDto> updateStage(@RequestHeader("TDM-Stage-Name") String name,
                                                     @RequestBody DataSourceDto ds)
    {
        // TODO
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/stage")
    public ResponseEntity<Void> deleteStage(@RequestHeader("TDM-Stage-Name") String name)
    {
        // TODO
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stage")
    public ResponseEntity<DataSourceDto> getStage(@RequestHeader("TDM-Stage-Name") String stageName)
    {
        DataSourceProxy ds = stageDsManager.getStageDataSourceByName(stageName);

        if(ds == null)
        {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(DataSourceMapper.toDto(ds));
    }

    @GetMapping("/stage/current/name")
    public ResponseEntity<String> getCurrentStageName()
    {
        String stageName = StageContextHolder.getStageName();

        if(stageName == null)
        {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stageName);
    }

    @GetMapping("/stage/current")
    public ResponseEntity<DataSourceDto> getCurrentStage()
    {
        return this.getStage(StageContextHolder.getStageName());
    }

    @PutMapping("/stage/current")
    public ResponseEntity<DataSourceDto> selectCurrentStage(@RequestHeader("TDM-Stage-Name") String stageName)
    {
        DataSourceProxy ds = stageDsManager.getStageDataSourceByName(stageName);

        if(ds == null)
        {
            return ResponseEntity.notFound().build();
        }

        StageContextHolder.setStageName(stageName);

        return ResponseEntity.ok(DataSourceMapper.toDto(ds));
    }

    @PutMapping("/stage/current/clear")
    public ResponseEntity<Void> clearCurrentStageSelection()
    {
        StageContextHolder.clearStageName();

        return ResponseEntity.noContent().build();
    }
}
