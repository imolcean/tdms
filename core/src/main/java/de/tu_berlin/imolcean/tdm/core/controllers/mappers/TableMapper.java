package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.ColumnDto;
import de.tu_berlin.imolcean.tdm.api.dto.TableDto;
import schemacrawler.schema.Table;

import java.util.List;
import java.util.stream.Collectors;

public class TableMapper
{
    public static TableDto toDto(Table table)
    {
        List<ColumnDto> columns = table.getColumns().stream()
                .map(ColumnMapper::toDto)
                .collect(Collectors.toList());

        return new TableDto(table.getName(), columns);
    }
}
