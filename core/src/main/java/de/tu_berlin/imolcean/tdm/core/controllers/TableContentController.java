package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import de.tu_berlin.imolcean.tdm.core.services.proxies.DataExportProxy;
import de.tu_berlin.imolcean.tdm.core.services.proxies.DataImportProxy;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableContentMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("api/data")
public class TableContentController
{
    private final DataSourceService dsService;
    private final SchemaService schemaService;
    private final TableContentService tableContentService;
    private final DataImportProxy dataImportProxy;
    private final DataExportProxy dataExportProxy;

    public TableContentController(DataSourceService dsService,
                                  SchemaService SchemaService,
                                  TableContentService tableContentService,
                                  DataImportProxy dataImportProxy,
                                  DataExportProxy dataExportProxy)
    {
        this.dsService = dsService;
        this.schemaService = SchemaService;
        this.tableContentService = tableContentService;
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
                        table,
                        tableContentService.getTableContent(ds, table)));
    }

    // TODO Remove and use only insertRows()?
//    @PostMapping("/{alias}/{table}")
//    public ResponseEntity<Void> insertRow(@PathVariable("alias") String alias,
//                                          @PathVariable("table") String tableName,
//                                          @RequestBody Object[] row) throws SQLException, SchemaCrawlerException
//    {
//        DataSource ds = dsService.getDataSourceByAlias(alias);
//        Table table = schemaService.getTable(ds, tableName);
//
//        tableContentService.insertRow(ds, table, row);
//
//        return ResponseEntity.noContent().build();
//    }

    @PostMapping("/{alias}/{table}")
    public ResponseEntity<Void> insertRows(@PathVariable("alias") String alias,
                                           @PathVariable("table") String tableName,
                                           @RequestBody List<Object[]> rows) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);
        Table table = schemaService.getTable(ds, tableName);

        tableContentService.insertRows(ds, table, rows);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{alias}/{table}/{row}")
    public ResponseEntity<Void> updateRow(@PathVariable("alias") String alias,
                                          @PathVariable("table") String tableName,
                                          @PathVariable("row") Integer rowIndex,
                                          @RequestBody Object[] row) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);
        Table table = schemaService.getTable(ds, tableName);

        tableContentService.updateRow(ds, table, rowIndex, row);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{alias}/{table}/{row}")
    public ResponseEntity<Void> deleteRowByIndex(@PathVariable("alias") String alias,
                                                 @PathVariable("table") String tableName,
                                                 @PathVariable("row") Integer rowIndex) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);
        Table table = schemaService.getTable(ds, tableName);

        tableContentService.deleteRow(ds, table, rowIndex);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("copy/{src_alias}/{target_alias}")
    public ResponseEntity<Void> copyAllData(@PathVariable("src_alias") String srcAlias,
                                            @PathVariable("target_alias") String targetAlias)
            throws SQLException, SchemaCrawlerException, IOException
    {
        DataSource src = dsService.getDataSourceByAlias(srcAlias);
        DataSource target = dsService.getDataSourceByAlias(targetAlias);

        Collection<Table> tables = schemaService.getSchema(src).getTables();

        tableContentService.copyData(src, target, tables);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{alias}/{table}")
    public ResponseEntity<Void> clearTable(@PathVariable("alias") String alias,
                                           @PathVariable("table") String tableName) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);
        Table table = schemaService.getTable(ds, tableName);

        tableContentService.clearTable(ds, table);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{alias}")
    public ResponseEntity<Void> clearAllTables(@PathVariable("alias") String alias)
            throws SQLException, IOException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);

        tableContentService.clearTables(ds, schemaService.getSchema(ds).getTables());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/import")
    public ResponseEntity<Void> importData() throws Exception
    {
        dataImportProxy.importData(dsService.getInternalDataSource());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/export")
    public ResponseEntity<Void> exportData() throws Exception
    {
        dataExportProxy.exportData(dsService.getInternalDataSource());

        return ResponseEntity.noContent().build();
    }
}
