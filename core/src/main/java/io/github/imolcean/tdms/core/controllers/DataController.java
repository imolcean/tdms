package io.github.imolcean.tdms.core.controllers;

import io.github.imolcean.tdms.api.dto.TableContentDto;
import io.github.imolcean.tdms.api.dto.TableRuleDto;
import io.github.imolcean.tdms.api.dto.ValueListDto;
import io.github.imolcean.tdms.core.controllers.mappers.ValueListMapper;
import io.github.imolcean.tdms.core.generation.ValueLibraryLoader;
import io.github.imolcean.tdms.core.services.ProjectService;
import io.github.imolcean.tdms.core.services.proxies.DataExportProxy;
import io.github.imolcean.tdms.core.services.proxies.DataGenerationProxy;
import io.github.imolcean.tdms.core.services.proxies.DataImportProxy;
import io.github.imolcean.tdms.core.services.DataSourceService;
import io.github.imolcean.tdms.api.services.SchemaService;
import io.github.imolcean.tdms.api.services.DataService;
import io.github.imolcean.tdms.core.controllers.mappers.TableContentMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/data")
public class DataController
{
    private final ProjectService projectService;
    private final DataSourceService dsService;
    private final SchemaService schemaService;
    private final DataService dataService;
    private final ValueLibraryLoader valueLibraryLoader;
    private final DataImportProxy dataImportProxy;
    private final DataExportProxy dataExportProxy;
    private final DataGenerationProxy dataGenerationProxy;

    public DataController(ProjectService projectService,
                          DataSourceService dsService,
                          SchemaService SchemaService,
                          DataService dataService,
                          ValueLibraryLoader valueLibraryLoader,
                          DataImportProxy dataImportProxy,
                          DataExportProxy dataExportProxy,
                          DataGenerationProxy dataGenerationProxy)
    {
        this.projectService = projectService;
        this.dsService = dsService;
        this.schemaService = SchemaService;
        this.dataService = dataService;
        this.valueLibraryLoader = valueLibraryLoader;
        this.dataImportProxy = dataImportProxy;
        this.dataExportProxy = dataExportProxy;
        this.dataGenerationProxy = dataGenerationProxy;
    }

    @GetMapping("/{alias}/{table}")
    public ResponseEntity<TableContentDto> getTableContent(@PathVariable("alias") String alias,
                                                           @PathVariable("table") String tableName) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);
        Table table = schemaService.getTable(ds, tableName);

        return ResponseEntity.ok(
                TableContentMapper.toDto(
                        table.getName(),
                        table.getColumns(),
                        dataService.getTableContent(ds, tableName)));
    }

    @GetMapping("/{alias}/{table}/{columns}")
    public ResponseEntity<TableContentDto> getTableContentForColumns(@PathVariable("alias") String alias,
                                                                     @PathVariable("table") String tableName,
                                                                     @PathVariable("columns") List<String> columnNames) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);
        Table table = schemaService.getTable(ds, tableName);

        List<Column> columns = table.getColumns().stream()
                .filter(column -> columnNames.contains(column.getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                TableContentMapper.toDto(
                        table.getName(),
                        columns,
                        dataService.getTableContentForColumns(ds, table, columns)));
    }

    @PostMapping("/internal/{table}")
    public ResponseEntity<Void> insertRows(@PathVariable("table") String tableName,
                                           @RequestBody List<Map<String, Object>> rows) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getInternalDataSource();
        Table table = schemaService.getTable(ds, tableName);

        List<Map<Column, Object>> _rows = rows.stream()
                .map(row -> columnNamesMap2ColumnMap(table, row))
                .collect(Collectors.toList());

        dataService.insertRows(ds, table, _rows);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/{table}/{row}")
    public ResponseEntity<Void> updateRow(@PathVariable("table") String tableName,
                                          @PathVariable("row") Integer rowIndex,
                                          @RequestBody Map<String, Object> row) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getInternalDataSource();
        Table table = schemaService.getTable(ds, tableName);

        dataService.updateRow(ds, table, rowIndex, columnNamesMap2ColumnMap(table, row));

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internal/{table}/{row}")
    public ResponseEntity<Void> deleteRowByIndex(@PathVariable("table") String tableName,
                                                 @PathVariable("row") Integer rowIndex) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getInternalDataSource();
        Table table = schemaService.getTable(ds, tableName);

        dataService.deleteRow(ds, table, rowIndex);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internal/{table}")
    public ResponseEntity<Void> clearTable(@PathVariable("table") String tableName) throws SQLException
    {
        DataSource ds = dsService.getInternalDataSource();

        dataService.clearTable(ds, tableName);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internal")
    public ResponseEntity<Void> clearAllTables()
            throws SQLException, IOException, SchemaCrawlerException
    {
        DataSource ds = dsService.getInternalDataSource();

        dataService.clearTables(ds, schemaService.getTableNames(ds));

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/import")
    public ResponseEntity<Void> importData() throws Exception
    {
        dataImportProxy.importData(dsService.getInternalDataSource(), projectService.getDataDir());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/export")
    public ResponseEntity<Void> exportData() throws Exception
    {
        dataExportProxy.exportData(dsService.getInternalDataSource(), projectService.getDataDir());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/generate")
    public ResponseEntity<Void> generateData(@RequestBody Collection<TableRuleDto> rules) throws Exception
    {
        dataGenerationProxy.generate(dsService.getInternalDataSource(), rules);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/generate/lists")
    public ResponseEntity<List<ValueListDto>> valueLists()
    {
        List<ValueListDto> valueLists =
                valueLibraryLoader.getLists().values().stream()
                .map(ValueListMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(valueLists);
    }

    private Map<Column, Object> columnNamesMap2ColumnMap(Table table, Map<String, Object> row)
    {
        Map<Column, Object> _row = new HashMap<>();
        for(String columnName : row.keySet())
        {
            schemaService.findColumn(table, columnName)
                    .ifPresent(column -> _row.put(column, row.get(column.getName())));
        }

        return _row;
    }
}
