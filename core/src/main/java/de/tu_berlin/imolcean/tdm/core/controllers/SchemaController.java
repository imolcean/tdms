package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDto;
import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.SchemaUpdateMapper;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableMetaDataMapper;
import de.tu_berlin.imolcean.tdm.core.services.SchemaUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/schema")
public class SchemaController
{
    private final DataSourceService dsService;
    private final SchemaService schemaService;
    private final SchemaUpdateService schemaUpdateService;

    public SchemaController(DataSourceService dsService,
                            SchemaService SchemaService,
                            SchemaUpdateService schemaUpdateService)
    {
        this.dsService = dsService;
        this.schemaService = SchemaService;
        this.schemaUpdateService = schemaUpdateService;
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

    @PutMapping("copy/{src_alias}/{target_alias}")
    public ResponseEntity<Void> copySchema(@PathVariable("src_alias") String srcAlias,
                                           @PathVariable("target_alias") String targetAlias) throws Exception
    {
        DataSource src = dsService.getDataSourceByAlias(srcAlias);
        DataSource target = dsService.getDataSourceByAlias(targetAlias);

        schemaService.copySchema(src, target);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{alias}")
    public ResponseEntity<Void> purgeSchema(@PathVariable("alias") String alias) throws Exception
    {
        schemaService.purgeSchema(dsService.getDataSourceByAlias(alias));

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

    @GetMapping("/tables/{alias}/empty")
    public ResponseEntity<List<String>> getEmptyTableNames(@PathVariable("alias") String alias) throws Exception
    {
        return ResponseEntity.ok(
                schemaService.getEmptyTableNames(
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

    @DeleteMapping("/table/{alias}/{table}")
    public ResponseEntity<Void> dropTable(@PathVariable("alias") String alias,
                                          @PathVariable("table") String tableName) throws SQLException, SchemaCrawlerException
    {
        schemaService.dropTable(dsService.getDataSourceByAlias(alias), tableName);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/update")
    public ResponseEntity<Boolean> isSchemaUpdateInProgress()
    {
        return ResponseEntity.ok(schemaUpdateService.isUpdateInProgress());
    }

    @PutMapping("/internal/update/init")
    public ResponseEntity<SchemaUpdateDto> initSchemaUpdate() throws Exception
    {
        SchemaUpdater.SchemaUpdateReport report =
                schemaUpdateService.initSchemaUpdate(dsService.getInternalDataSource(), dsService.getTmpDataSource());

        return ResponseEntity.ok(SchemaUpdateMapper.toDto(report));
    }

    @PutMapping("/internal/update/commit")
    public ResponseEntity<Void> commitSchemaUpdate(@RequestBody SchemaUpdateCommitRequest request) throws Exception
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
