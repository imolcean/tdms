package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import schemacrawler.schema.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TableMetaDataMapper
{
    public static TableMetaDataDto toDto(Table table)
    {
        List<TableMetaDataDto.Column> columns = table.getColumns().stream()
                .map(TableMetaDataMapper::mapColumn)
                .collect(Collectors.toList());

        TableMetaDataDto.PrimaryKey pk = null;

        if(table.hasPrimaryKey())
        {
            pk = mapPk(table.getPrimaryKey());
        }

        List<TableMetaDataDto.ForeignKey> fks = mapFks(table.getImportedForeignKeys());

        return new TableMetaDataDto(table.getName(), columns, pk, fks);
    }

    private static TableMetaDataDto.Column mapColumn(Column column)
    {
        return new TableMetaDataDto.Column(
                column.getName(),
                column.getColumnDataType().getName(),
                column.isNullable());
    }

    private static TableMetaDataDto.PrimaryKey mapPk(PrimaryKey pk)
    {
        List<String> columnNames = pk.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.toList());

        return new TableMetaDataDto.PrimaryKey(pk.getName(), columnNames);
    }

    private static List<TableMetaDataDto.ForeignKey> mapFks(Collection<ForeignKey> fks)
    {
        List<TableMetaDataDto.ForeignKey> list = new ArrayList<>();

        for(ForeignKey fk : fks)
        {
            List<String> columnNames = new ArrayList<>();
            List<String> pkColumnNames = new ArrayList<>();
            String pkTable = "";

            for(ForeignKeyColumnReference ref : fk.getColumnReferences())
            {
                columnNames.add(ref.getForeignKeyColumn().getName());
                pkColumnNames.add(ref.getPrimaryKeyColumn().getName());

                if(pkTable.isBlank())
                {
                    pkTable = ref.getPrimaryKeyColumn().getParent().getName();
                }
            }

            list.add(new TableMetaDataDto.ForeignKey(fk.getSpecificName(), columnNames, pkTable, pkColumnNames));
        }

        return list;
    }
}
