package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.SchemaService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableMetaDataMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    public SchemaController(DataSourceService dsService, SchemaService schemaService)
    {
        this.dsService = dsService;
        this.schemaService = schemaService;
    }

    @GetMapping("/")
    public ResponseEntity<List<TableMetaDataDto>> getSchema(@RequestHeader("TDM-Datasource-Alias") String alias)
            throws SQLException, SchemaCrawlerException
    {
        List<TableMetaDataDto> list = schemaService
                .getSchema(dsService.getDataSourceByAlias(alias))
                .getTables().stream()
                .map(TableMetaDataMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/tables")
    public ResponseEntity<List<String>> getTableNames(@RequestHeader("TDM-Datasource-Alias") String alias)
            throws Exception
    {
        return ResponseEntity.ok(
                schemaService.getTableNames(
                        dsService.getDataSourceByAlias(alias)));
    }

    @GetMapping("/tables/occupied")
    public ResponseEntity<List<String>> getOccupiedTableNames(@RequestHeader("TDM-Datasource-Alias") String alias) throws Exception
    {
        return ResponseEntity.ok(
                schemaService.getOccupiedTableNames(
                        dsService.getDataSourceByAlias(alias)));
    }

    @GetMapping("/table")
    public ResponseEntity<TableMetaDataDto> getTable(@RequestHeader("TDM-Datasource-Alias") String alias,
                                                     @RequestHeader("TDM-Table-Name") String tableName) throws SQLException, SchemaCrawlerException
    {
        return ResponseEntity.ok(
                TableMetaDataMapper.toDto(
                        schemaService.getTable(
                                dsService.getDataSourceByAlias(alias),
                                tableName)));
    }
}
