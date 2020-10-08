package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import java.util.List;
import java.util.stream.Collectors;

public class TableContentMapper
{
   public static TableContentDto toDto(String tableName, List<Column> columns, List<Object[]> rows)
   {
       return new TableContentDto(
               tableName,
               columns.stream()
                       .map(NamedObject::getName)
                       .collect(Collectors.toList()),
               rows);
   }
}
