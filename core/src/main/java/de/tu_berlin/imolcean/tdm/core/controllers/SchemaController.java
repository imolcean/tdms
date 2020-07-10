package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.SchemaService;
import de.tu_berlin.imolcean.tdm.core.SchemaUpdaterService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableMetaDataMapper;
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
                            SchemaService schemaService,
                            SchemaUpdaterService schemaUpdaterService)
    {
        this.dsService = dsService;
        this.schemaService = schemaService;
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

    @PutMapping("/internal")
    public ResponseEntity<Void> updateSchemaInternal() throws Exception
    {
        SchemaUpdater updater = schemaUpdaterService.getSelectedSchemaUpdater()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        updater.updateSchema(dsService.getInternalDataSource());

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
