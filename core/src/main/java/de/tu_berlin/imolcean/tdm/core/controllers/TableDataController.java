package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableDataDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.TableDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("api/data")
public class TableDataController
{
    private final DataSourceService dsService;

    private final TableDataService tableDataService;

    public TableDataController(DataSourceService dsService, TableDataService tableDataService)
    {
        this.dsService = dsService;
        this.tableDataService = tableDataService;
    }

    @GetMapping("/")
    public ResponseEntity<TableDataDto> getTableContent(@RequestHeader("TDM-Datasource-Alias") String alias,
                                                        @RequestHeader("TDM-Table-Name") String tableName) throws SQLException
    {
        return ResponseEntity.ok(
                tableDataService.getTableData(dsService.getDataSourceByAlias(alias), tableName));
    }

//    @PostMapping("/")
    public ResponseEntity<Void> insertRow(@RequestHeader("TDM-Datasource-Alias") String alias,
                                          @RequestHeader("TDM-Table-Name") String tableName,
                                          @RequestBody Object[] row) throws SQLException
    {
        tableDataService.insertRow(dsService.getDataSourceByAlias(alias), tableName, row);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/")
    public ResponseEntity<Void> updateRow(@RequestHeader("TDM-Datasource-Alias") String alias,
                                          @RequestHeader("TDM-Table-Name") String tableName,
                                          @RequestHeader("TDM-Row-Index") Integer rowIndex,
                                          @RequestBody Object[] row) throws SQLException
    {
        tableDataService.updateRow(dsService.getDataSourceByAlias(alias), tableName, rowIndex, row);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> deleteRow(@RequestHeader("TDM-Datasource-Alias") String alias,
                                          @RequestHeader("TDM-Table-Name") String tableName,
                                          @RequestHeader("TDM-Row-Index") Integer rowIndex) throws SQLException
    {
        tableDataService.deleteRow(dsService.getDataSourceByAlias(alias), tableName, rowIndex);

        return ResponseEntity.noContent().build();
    }
}
