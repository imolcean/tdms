package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.api.dto.StageDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.StageDataSourceAlreadyExistsException;
import de.tu_berlin.imolcean.tdm.api.exceptions.StageDataSourceNotFoundException;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.StageMapper;
import de.tu_berlin.imolcean.tdm.core.repositories.StageDataSourceRepository;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.DataSourceMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/datasource")
public class DataSourceController
{
    private final StageDataSourceRepository stageDsRepo;
    private final DataSourceService dsService;

    public DataSourceController(StageDataSourceRepository stageDsRepo, DataSourceService dsService)
    {
        this.stageDsRepo = stageDsRepo;
        this.dsService = dsService;
    }

    @GetMapping("/internal")
    public DataSourceDto getInternal()
    {
        return DataSourceMapper.toDto(dsService.getInternalDataSource());
    }

    @GetMapping("/tmp")
    public DataSourceDto getTmp()
    {
        return DataSourceMapper.toDto(dsService.getTmpDataSource());
    }

    @GetMapping("/stages")
    public List<StageDto> getAllStages()
    {
        return stageDsRepo.findAllAsMap().entrySet().stream()
                .map(entry -> StageMapper.toDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @PostMapping("/stages/{name}")
    public ResponseEntity<StageDto> createStage(@PathVariable("name") String name,
                                                @RequestBody DataSourceDto dto)
    {
        if(stageDsRepo.existsById(name))
        {
            throw new StageDataSourceAlreadyExistsException(name);
        }

        return ResponseEntity.ok(StageMapper.toDto(name, stageDsRepo.save(name, dto)));
    }

    @PutMapping("/stage/{name}")
    public ResponseEntity<StageDto> updateStage(@PathVariable("name") String name,
                                                @RequestBody DataSourceDto dto)
    {
        if(!stageDsRepo.existsById(name))
        {
            throw new StageDataSourceNotFoundException(name);
        }

        return ResponseEntity.ok(StageMapper.toDto(name, stageDsRepo.save(name, dto)));
    }

    @DeleteMapping("/stage/{name}")
    public ResponseEntity<Void> deleteStage(@PathVariable("name") String name)
    {
        stageDsRepo.deleteById(name);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stage/{name}")
    public ResponseEntity<StageDto> getStage(@PathVariable("name") String stageName)
    {
        return ResponseEntity.ok(
                StageMapper.toDto(
                        stageName,
                        stageDsRepo.findById(stageName)
                                .orElseThrow(() -> new StageDataSourceNotFoundException(stageName))));
    }

    @GetMapping("/stage/current")
    public ResponseEntity<DataSourceDto> getCurrentStageDatasource()
    {
        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.getCurrentStageDataSource()));
    }
}
