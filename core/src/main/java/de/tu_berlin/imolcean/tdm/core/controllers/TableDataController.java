package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableDataDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.TableDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
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
    public ResponseEntity<TableDataDto> getTableContent(@RequestHeader("TDM-Datasource-Name") String dsName,
                                                        @RequestHeader("TDM-Table-Name") String tableName) throws SQLException
    {
        DataSource ds;
        try
        {
            ds = dsService.getDataSourceByName(dsName);
        }
        catch(Exception e)
        {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tableDataService.getTableData(ds, tableName));
    }

//    @PostMapping("/")
    public ResponseEntity<Void> insertRow(@RequestHeader("TDM-Datasource-Name") String dsName,
                                              @RequestHeader("TDM-Table-Name") String tableName,
                                              @RequestBody Object[] row) throws SQLException
    {
        DataSource ds;
        try
        {
            ds = dsService.getDataSourceByName(dsName);
        }
        catch(Exception e)
        {
            return ResponseEntity.notFound().build();
        }

        tableDataService.insertRow(ds, tableName, row);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/")
    public ResponseEntity<Void> updateRow(@RequestHeader("TDM-Datasource-Name") String dsName,
                                              @RequestHeader("TDM-Table-Name") String tableName,
                                              @RequestHeader("TDM-Row-Index") Integer rowIndex,
                                              @RequestBody Object[] row) throws SQLException
    {
        DataSource ds;
        try
        {
            ds = dsService.getDataSourceByName(dsName);
        }
        catch(Exception e)
        {
            return ResponseEntity.notFound().build();
        }

        tableDataService.updateRow(ds, tableName, rowIndex, row);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> deleteRow(@RequestHeader("TDM-Datasource-Name") String dsName,
                                       @RequestHeader("TDM-Table-Name") String tableName,
                                       @RequestHeader("TDM-Row-Index") Integer rowIndex) throws SQLException
    {
        DataSource ds;
        try
        {
            ds = dsService.getDataSourceByName(dsName);
        }
        catch(Exception e)
        {
            return ResponseEntity.notFound().build();
        }

        tableDataService.deleteRow(ds, tableName, rowIndex);

        return ResponseEntity.noContent().build();
    }
}
