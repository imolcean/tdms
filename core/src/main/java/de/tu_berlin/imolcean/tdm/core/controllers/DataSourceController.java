package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
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

    @PostMapping("/stages/{name}")
    public ResponseEntity<DataSourceDto> createStage(@PathVariable("name") String name,
                                                     @RequestBody DataSourceDto dto)
    {
        StageDataSourceParams params = new StageDataSourceParams(
                name,
                dto.getDriverClassName(),
                dto.getUrl(),
                dto.getUsername(),
                dto.getPassword());

        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.storeStageDsParams(params)));
    }

    @PutMapping("/stage/{name}")
    public ResponseEntity<DataSourceDto> updateStage(@PathVariable("name") String name,
                                                     @RequestBody DataSourceDto dto)
    {
        StageDataSourceParams params = new StageDataSourceParams(
                name,
                dto.getDriverClassName(),
                dto.getUrl(),
                dto.getUsername(),
                dto.getPassword());

        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.updateStageDataSource(params)));
    }

    @DeleteMapping("/stage/{name}")
    public ResponseEntity<Void> deleteStage(@PathVariable("name") String name)
    {
        dsService.deleteStageDataSource(name);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stage/{name}")
    public ResponseEntity<DataSourceDto> getStage(@PathVariable("name") String stageName)
    {
        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.getStageDataSourceByName(stageName)));
    }

    @GetMapping("/stage/current")
    public ResponseEntity<DataSourceDto> getCurrentStage()
    {
        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.getCurrentStageDataSource()));
    }
}
