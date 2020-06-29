package de.tu_berlin.imolcean.tdm.core.controllers;

import com.google.common.collect.Table;
import de.tu_berlin.imolcean.tdm.api.dto.TableDataDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
import de.tu_berlin.imolcean.tdm.core.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.StageDataSourceManager;
import de.tu_berlin.imolcean.tdm.core.TableDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                                                        @RequestHeader("TDM-Table-Name") String tableName)
            throws SQLException
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

    // TODO CrUD rows
}
