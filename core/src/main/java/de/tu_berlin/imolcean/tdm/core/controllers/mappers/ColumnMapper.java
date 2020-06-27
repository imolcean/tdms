package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.ColumnDto;
import schemacrawler.schema.Column;

public class ColumnMapper
{
    public static ColumnDto toDto(Column column)
    {
        return new ColumnDto(
                column.getName(),
                column.getColumnDataType().getName(),
                column.isPartOfPrimaryKey());
    }
}
