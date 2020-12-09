package io.github.imolcean.tdms.core.controllers;

import io.github.imolcean.tdms.api.dto.SchemaUpdateDataMappingRequest;
import io.github.imolcean.tdms.api.dto.SchemaUpdateDto;
import io.github.imolcean.tdms.api.dto.TableMetaDataDto;
import io.github.imolcean.tdms.api.interfaces.updater.SchemaUpdater;
import io.github.imolcean.tdms.core.controllers.mappers.SchemaUpdateMapper;
import io.github.imolcean.tdms.core.services.DataSourceService;
import io.github.imolcean.tdms.api.services.SchemaService;
import io.github.imolcean.tdms.core.controllers.mappers.TableMetaDataMapper;
import io.github.imolcean.tdms.core.services.proxies.SchemaUpdateProxy;
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
    private final SchemaUpdateProxy schemaUpdateProxy;

    public SchemaController(DataSourceService dsService,
                            SchemaService SchemaService,
                            SchemaUpdateProxy schemaUpdateProxy)
    {
        this.dsService = dsService;
        this.schemaService = SchemaService;
        this.schemaUpdateProxy = schemaUpdateProxy;
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
        return ResponseEntity.ok(schemaUpdateProxy.isUpdateInProgress());
    }

    @PutMapping("/internal/update/init")
    public ResponseEntity<SchemaUpdateDto> initSchemaUpdate() throws Exception
    {
        SchemaUpdater.SchemaUpdateReport report =
                schemaUpdateProxy.initSchemaUpdate(dsService.getInternalDataSource(), dsService.getTmpDataSource());

        return ResponseEntity.ok(SchemaUpdateMapper.toDto(report));
    }

    @PutMapping("/internal/update/data/map")
    public ResponseEntity<Void> schemaUpdateMapData(@RequestBody SchemaUpdateDataMappingRequest request) throws Exception
    {
        schemaUpdateProxy.mapData(request);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/update/data/rollback")
    public ResponseEntity<Void> schemaUpdateRollbackDataMapping() throws Exception
    {
        schemaUpdateProxy.rollbackDataMapping();

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/update/commit")
    public ResponseEntity<Void> commitSchemaUpdate() throws Exception
    {
        schemaUpdateProxy.commitSchemaUpdate();

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/update/cancel")
    public ResponseEntity<Void> cancelSchemaUpdate() throws Exception
    {
        schemaUpdateProxy.cancelSchemaUpdate();

        return ResponseEntity.noContent().build();
    }
}
