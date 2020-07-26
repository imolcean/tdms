package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableContentMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
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

    public TableContentController(DataSourceService dsService, SchemaService SchemaService, TableContentService tableContentService)
    {
        this.dsService = dsService;
        this.schemaService = SchemaService;
        this.tableContentService = tableContentService;
    }

    @GetMapping("/{alias}/{table}")
    public ResponseEntity<TableContentDto> getTableContent(@PathVariable("alias") String alias,
                                                           @PathVariable("table") String tableName) throws SQLException, SchemaCrawlerException
    {
        DataSource ds = dsService.getDataSourceByAlias(alias);
        Table table = schemaService.getTable(ds, tableName);

        return ResponseEntity.ok(
                TableContentMapper.toDto(
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

    @PutMapping("/{src_alias}/{target_alias}")
    public ResponseEntity<Void> copyAllData(@PathVariable("src_alias") String srcAlias,
                                            @PathVariable("target_alias") String targetAlias) throws SQLException, SchemaCrawlerException
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
}
