package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoCurrentStageException;
import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
import de.tu_berlin.imolcean.tdm.core.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.StageContextHolder;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.DataSourceMapper;
import de.tu_berlin.imolcean.tdm.core.entities.StageDataSourceParams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/datasource")
public class DataSourceController
{
    // TODO Move some parts to StageController

    private final DataSourceService dsService;

    public DataSourceController(DataSourceService dsService)
    {
        this.dsService = dsService;
    }

    @GetMapping("/internal")
    public DataSourceDto getInternal()
    {
        return DataSourceMapper.toDto(dsService.getInternalDataSource());
    }

    @GetMapping("/stages")
    public Map<String, DataSourceDto> getAllStages()
    {
        return dsService.getAllStagesDataSources().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> DataSourceMapper.toDto(entry.getValue())));
    }

    @PostMapping("/stages")
    public ResponseEntity<DataSourceDto> createStage(@RequestHeader("TDM-Stage-Name") String name,
                                                     @RequestBody DataSourceDto dto)
    {
        StageDataSourceParams params = new StageDataSourceParams(
                name,
                dto.getDriverClassName(),
                dto.getUrl(),
                dto.getUser(),
                dto.getPassword());

        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.storeStageDsParams(params)));
    }

    @PutMapping("/stage")
    public ResponseEntity<DataSourceDto> updateStage(@RequestHeader("TDM-Stage-Name") String name,
                                                     @RequestBody DataSourceDto dto)
    {
        StageDataSourceParams params = new StageDataSourceParams(
                name,
                dto.getDriverClassName(),
                dto.getUrl(),
                dto.getUser(),
                dto.getPassword());

        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.updateStageDataSource(params)));
    }

    @DeleteMapping("/stage")
    public ResponseEntity<Void> deleteStage(@RequestHeader("TDM-Stage-Name") String name)
    {
        dsService.deleteStageDataSource(name);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stage")
    public ResponseEntity<DataSourceDto> getStage(@RequestHeader("TDM-Stage-Name") String stageName)
    {
        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.getStageDataSourceByName(stageName)));
    }

    @GetMapping("/stage/current/name")
    public ResponseEntity<String> getCurrentStageName()
    {
        String stageName = StageContextHolder.getStageName();

        if(stageName == null)
        {
            throw new NoCurrentStageException();
        }

        return ResponseEntity.ok(stageName);
    }

    @GetMapping("/stage/current")
    public ResponseEntity<DataSourceDto> getCurrentStage()
    {
        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.getCurrentStageDataSource()));
    }

    @PutMapping("/stage/current")
    public ResponseEntity<DataSourceDto> selectCurrentStage(@RequestHeader("TDM-Stage-Name") String stageName)
    {
        DataSourceProxy ds = dsService.getStageDataSourceByName(stageName);

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
