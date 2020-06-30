package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.SchemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.sql.SQLException;
import java.util.*;

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
    public ResponseEntity<List<TableMetaDataDto>> getStageSchema(@RequestHeader("TDM-Datasource-Alias") String dsName)
            throws SQLException, SchemaCrawlerException
    {
        return ResponseEntity.ok(
                schemaService.getSchema(
                        dsService.getDataSourceByAlias(dsName)));
    }
}
