package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDto;
import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.SchemaUpdateMapper;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableMetaDataMapper;
import de.tu_berlin.imolcean.tdm.core.services.SchemaUpdaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/schema")
public class SchemaController
{
    private final DataSourceService dsService;
    private final SchemaService schemaService;
    private final SchemaUpdaterService schemaUpdaterService;

    public SchemaController(DataSourceService dsService,
                            SchemaService SchemaService,
                            SchemaUpdaterService schemaUpdaterService)
    {
        this.dsService = dsService;
        this.schemaService = SchemaService;
        this.schemaUpdaterService = schemaUpdaterService;
    }

    @GetMapping("/{alias}")
    public ResponseEntity<List<TableMetaDataDto>> getSchema(@PathVariable("alias") String alias)
            throws SQLException, SchemaCrawlerException
    {
        List<TableMetaDataDto> list = schemaService
                .getSchema(dsService.getDataSourceByAlias(alias))
                .getTables().stream()
                .map(TableMetaDataMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/internal/update")
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

    // TODO Take a DTO
    @PutMapping("/internal/update/commit")
    public ResponseEntity<Void> commitUpdateSchemaInternal() throws Exception
    {
        SchemaUpdater updater = schemaUpdaterService.getSelectedSchemaUpdater()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        updater.commitSchemaUpdate(null);

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

    @GetMapping("/tables/{alias}")
    public ResponseEntity<List<String>> getTableNames(@PathVariable("alias") String alias)
            throws Exception
    {
        return ResponseEntity.ok(
                schemaService.getTableNames(
                        dsService.getDataSourceByAlias(alias)));
    }

    @GetMapping("/tables/{alias}/occupied")
    public ResponseEntity<List<String>> getOccupiedTableNames(@PathVariable("alias") String alias) throws Exception
    {
        return ResponseEntity.ok(
                schemaService.getOccupiedTableNames(
                        dsService.getDataSourceByAlias(alias)));
    }

    @GetMapping("/table/{alias}/{table}")
    public ResponseEntity<TableMetaDataDto> getTable(@PathVariable("alias") String alias,
                                                     @PathVariable("table") String tableName) throws SQLException, SchemaCrawlerException
    {
        return ResponseEntity.ok(
                TableMetaDataMapper.toDto(
                        schemaService.getTable(
                                dsService.getDataSourceByAlias(alias),
                                tableName)));
    }
}
