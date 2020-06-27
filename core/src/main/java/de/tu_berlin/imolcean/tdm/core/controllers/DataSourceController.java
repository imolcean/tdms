package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
import de.tu_berlin.imolcean.tdm.core.StageContextHolder;
import de.tu_berlin.imolcean.tdm.core.StageDataSourceManager;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.DataSourceMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
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

//    @PostMapping("/stages")
    public ResponseEntity<DataSourceDto> createStage(@RequestHeader("TDM-Stage-Name") String name,
                                     @RequestBody DataSourceDto ds)
    {
        try
        {
            return ResponseEntity.ok(
                    DataSourceMapper.toDto(
                            stageDsManager.createStageDataSource(name, ds)));
        }
        catch(FileAlreadyExistsException e)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

//    @PutMapping("/stage")
    public ResponseEntity<DataSourceDto> updateStage(@RequestHeader("TDM-Stage-Name") String name,
                                                     @RequestBody DataSourceDto ds)
    {
        try
        {
            return ResponseEntity.ok(
                    DataSourceMapper.toDto(
                            stageDsManager.updateStageDataSource(name, ds)));
        }
        catch(FileNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
    }

//    @DeleteMapping("/stage")
    public ResponseEntity<Void> deleteStage(@RequestHeader("TDM-Stage-Name") String name)
    {
        try
        {
            stageDsManager.deleteStageDataSource(name);
            return ResponseEntity.noContent().build();
        }
        catch(FileNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
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
