package io.github.imolcean.tdms.core.controllers;

import io.github.imolcean.tdms.api.dto.DataSourceDto;
import io.github.imolcean.tdms.api.dto.StageDto;
import io.github.imolcean.tdms.api.exceptions.StageDataSourceAlreadyExistsException;
import io.github.imolcean.tdms.api.exceptions.StageDataSourceNotFoundException;
import io.github.imolcean.tdms.core.controllers.mappers.StageMapper;
import io.github.imolcean.tdms.core.repositories.StageDataSourceRepository;
import io.github.imolcean.tdms.core.services.DataSourceService;
import io.github.imolcean.tdms.core.controllers.mappers.DataSourceMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
