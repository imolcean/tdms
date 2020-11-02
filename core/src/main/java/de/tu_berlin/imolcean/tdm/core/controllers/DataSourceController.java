package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.StageDataSourceAlreadyExistsException;
import de.tu_berlin.imolcean.tdm.api.exceptions.StageDataSourceNotFoundException;
import de.tu_berlin.imolcean.tdm.core.repositories.StageDataSourceRepository;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.DataSourceMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public Map<String, DataSourceDto> getAllStages()
    {
        return stageDsRepo.findAllAsMap();
    }

    @PostMapping("/stages/{name}")
    public ResponseEntity<DataSourceDto> createStage(@PathVariable("name") String name,
                                                     @RequestBody DataSourceDto dto)
    {
        if(stageDsRepo.existsById(name))
        {
            throw new StageDataSourceAlreadyExistsException(name);
        }

        return ResponseEntity.ok(stageDsRepo.save(name, dto));
    }

    @PutMapping("/stage/{name}")
    public ResponseEntity<DataSourceDto> updateStage(@PathVariable("name") String name,
                                                     @RequestBody DataSourceDto dto)
    {
        if(!stageDsRepo.existsById(name))
        {
            throw new StageDataSourceNotFoundException(name);
        }

        return ResponseEntity.ok(stageDsRepo.save(name, dto));
    }

    @DeleteMapping("/stage/{name}")
    public ResponseEntity<Void> deleteStage(@PathVariable("name") String name)
    {
        stageDsRepo.deleteById(name);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stage/{name}")
    public ResponseEntity<DataSourceDto> getStage(@PathVariable("name") String stageName)
    {
        return ResponseEntity.ok(
                stageDsRepo.findById(stageName)
                        .orElseThrow(() -> new StageDataSourceNotFoundException(stageName)));
    }

    @GetMapping("/stage/current")
    public ResponseEntity<DataSourceDto> getCurrentStage()
    {
        return ResponseEntity.ok(
                DataSourceMapper.toDto(
                        dsService.getCurrentStageDataSource()));
    }
}
