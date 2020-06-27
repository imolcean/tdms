package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.dto.ColumnDto;
import de.tu_berlin.imolcean.tdm.api.dto.TableDto;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.ColumnMapper;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.TableMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemaService
{
    // TODO Cache Catalogs from SchemaExtractor

    private final DataSourceProxy internalDs;

    private final StageDataSourceManager stageDsManager;

    private final SchemaExtractor schemaExtractor;

    public SchemaService(@Qualifier("InternalDataSource") DataSourceProxy internalDs,
                         StageDataSourceManager stageDsManager,
                         SchemaExtractor schemaExtractor)
    {
        this.internalDs = internalDs;
        this.stageDsManager = stageDsManager;
        this.schemaExtractor = schemaExtractor;
    }

    public List<TableDto> getSchema(DataSource ds) throws Exception
    {
        return schemaExtractor.extractDboTables(ds).getTables().stream()
                .map(TableMapper::toDto)
                .collect(Collectors.toList());
    }
}
