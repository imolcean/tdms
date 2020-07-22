package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.SchemaUpdateMapper;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.services.SchemaUpdaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/schema/internal/update")
public class SchemaUpdateController
{
    private final DataSourceService dsService;
    private final SchemaService schemaService;
    private final SchemaUpdaterService schemaUpdaterService;

    public SchemaUpdateController(DataSourceService dsService,
                                  SchemaService schemaService,
                                  SchemaUpdaterService schemaUpdaterService)
    {
        this.dsService = dsService;
        this.schemaService = schemaService;
        this.schemaUpdaterService = schemaUpdaterService;
    }

    @GetMapping("/")
    public ResponseEntity<Boolean> isSchemaUpdateInProgress()
    {
        SchemaUpdater updater = schemaUpdaterService.getSelectedSchemaUpdater()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        return ResponseEntity.ok(updater.isUpdateInProgress());
    }

    @PutMapping("/internal/update/init")
    public ResponseEntity<SchemaUpdateDto> initUpdateSchemaInternal() throws Exception
    {
        SchemaUpdater updater = schemaUpdaterService.getSelectedSchemaUpdater()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        updater.setSchemaService(schemaService);

        SchemaUpdater.SchemaUpdate update = updater.initSchemaUpdate(dsService.getInternalDataSource(), dsService.getTmpDataSource());

        return ResponseEntity.ok(SchemaUpdateMapper.toDto(update));
    }

    @PutMapping("/internal/update/commit")
    public ResponseEntity<Void> commitUpdateSchemaInternal(@RequestBody SchemaUpdateCommitRequest request) throws Exception
    {
        SchemaUpdater updater = schemaUpdaterService.getSelectedSchemaUpdater()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        updater.commitSchemaUpdate(request);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/update/cancel")
    public ResponseEntity<Void> cancelSchemaUpdate() throws Exception
    {
        SchemaUpdater updater = schemaUpdaterService.getSelectedSchemaUpdater()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        updater.cancelSchemaUpdate();

        return ResponseEntity.noContent().build();
    }
}
