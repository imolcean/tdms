package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;
import de.tu_berlin.imolcean.tdm.core.StageDataSourceManager;
import de.tu_berlin.imolcean.tdm.core.SchemaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("api/schema")
public class SchemaController
{
    private final DataSourceProxy internalDs;

    private final StageDataSourceManager stageDsManager;

    private final SchemaService schemaService;

    public SchemaController(@Qualifier("InternalDataSource") DataSourceProxy internalDs,
                            StageDataSourceManager stageDsManager,
                            SchemaService schemaService)
    {
        this.internalDs = internalDs;
        this.stageDsManager = stageDsManager;
        this.schemaService = schemaService;
    }

    @GetMapping("/internal")
    public ResponseEntity<List<TableDto>> getInternalSchema(@RequestHeader("TDM-Datasource-Name") String datasourceName) throws Exception
    {
        return ResponseEntity.ok(schemaService.getSchema(internalDs));
    }

    @GetMapping("/stage")
    public ResponseEntity<List<TableDto>> getStageSchema(@RequestHeader("TDM-Datasource-Name") String datasourceName) throws Exception
    {
        DataSourceProxy ds = stageDsManager.getStageDataSourceByName(datasourceName);

        if(ds == null)
        {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(schemaService.getSchema(ds));
    }
}
