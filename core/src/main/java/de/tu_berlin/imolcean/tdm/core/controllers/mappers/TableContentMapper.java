package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import java.util.List;
import java.util.stream.Collectors;

public class TableContentMapper
{
    public static TableContentDto toDto(Table table, List<Object[]> rows)
    {
        return new TableContentDto(
                table.getName(),
                table.getColumns().stream()
                        .map(NamedObject::getName)
                        .collect(Collectors.toList()),
                rows);
    }
}
