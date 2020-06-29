package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import java.util.List;
import java.util.stream.Collectors;

public class TableMetaDataMapper
{
    public static TableMetaDataDto toDto(Table table)
    {
        List<TableMetaDataDto.Column> columns = table.getColumns().stream()
                .map(TableMetaDataMapper::mapColumn)
                .collect(Collectors.toList());

        return new TableMetaDataDto(table.getName(), columns);
    }

    private static TableMetaDataDto.Column mapColumn(Column column)
    {
        return new TableMetaDataDto.Column(
                column.getName(),
                column.getColumnDataType().getName(),
                column.isNullable(),
                column.isPartOfPrimaryKey());
    }
}
