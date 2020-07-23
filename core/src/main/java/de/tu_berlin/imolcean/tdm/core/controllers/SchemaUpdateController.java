package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.SchemaUpdateMapper;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.services.SchemaUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/schema/internal/update")
public class SchemaUpdateController
{
    private final DataSourceService dsService;
    private final SchemaUpdateService schemaUpdateService;

    public SchemaUpdateController(DataSourceService dsService,
                                  SchemaUpdateService schemaUpdateService)
    {
        this.dsService = dsService;
        this.schemaUpdateService = schemaUpdateService;
    }

    @GetMapping("/")
    public ResponseEntity<Boolean> isSchemaUpdateInProgress()
    {
        return ResponseEntity.ok(schemaUpdateService.isUpdateInProgress());
    }

    @PutMapping("/internal/update/init")
    public ResponseEntity<SchemaUpdateDto> initUpdateSchemaInternal() throws Exception
    {
        SchemaUpdater.SchemaUpdateReport report =
                schemaUpdateService.initSchemaUpdate(dsService.getInternalDataSource(), dsService.getTmpDataSource());

        return ResponseEntity.ok(SchemaUpdateMapper.toDto(report));
    }

    @PutMapping("/internal/update/commit")
    public ResponseEntity<Void> commitUpdateSchemaInternal(@RequestBody SchemaUpdateCommitRequest request) throws Exception
    {
        schemaUpdateService.commitSchemaUpdate(request);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/update/cancel")
    public ResponseEntity<Void> cancelSchemaUpdate() throws Exception
    {
        schemaUpdateService.cancelSchemaUpdate();

        return ResponseEntity.noContent().build();
    }
}
