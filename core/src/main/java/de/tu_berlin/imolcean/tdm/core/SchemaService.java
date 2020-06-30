package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableMetaDataMapper;
import org.springframework.stereotype.Service;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemaService
{
    // TODO Cache Catalogs from SchemaExtractor

    private final SchemaExtractor schemaExtractor;

    public SchemaService(SchemaExtractor schemaExtractor)
    {
        this.schemaExtractor = schemaExtractor;
    }

    public List<TableMetaDataDto> getSchema(DataSource ds) throws SQLException, SchemaCrawlerException
    {
        return schemaExtractor.extractDboTables(ds).getTables().stream()
                .map(TableMetaDataMapper::toDto)
                .collect(Collectors.toList());
    }
}
