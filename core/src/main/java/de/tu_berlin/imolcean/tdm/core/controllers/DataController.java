package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import de.tu_berlin.imolcean.tdm.core.services.proxies.DataExportProxy;
import de.tu_berlin.imolcean.tdm.core.services.proxies.DataImportProxy;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.DataService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableContentMapper;
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
    private final DataImportProxy dataImportProxy;
    private final DataExportProxy dataExportProxy;

    public DataController(ProjectService projectService,
                          DataSourceService dsService,
                          SchemaService SchemaService,
                          DataService dataService,
                          DataImportProxy dataImportProxy,
                          DataExportProxy dataExportProxy)
    {
        this.projectService = projectService;
        this.dsService = dsService;
        this.schemaService = SchemaService;
        this.dataService = dataService;
        this.dataImportProxy = dataImportProxy;
        this.dataExportProxy = dataExportProxy;
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
                        dataService.getTableContent(ds, table)));
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
    public ResponseEntity<Void> clearTable(@PathVariable("table") String tableName) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getInternalDataSource();
        Table table = schemaService.getTable(ds, tableName);

        dataService.clearTable(ds, table);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internal")
    public ResponseEntity<Void> clearAllTables()
            throws SQLException, IOException, SchemaCrawlerException
    {
        DataSource ds = dsService.getInternalDataSource();

        dataService.clearTables(ds, schemaService.getSchema(ds).getTables());

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
