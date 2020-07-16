package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateDto;
import de.tu_berlin.imolcean.tdm.api.dto.TableMetaDataDto;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;

import java.util.List;
import java.util.stream.Collectors;

public class SchemaUpdateMapper
{
    public static SchemaUpdateDto toDto(SchemaUpdater.SchemaUpdate update)
    {
        List<TableMetaDataDto> addedTables = update.getAddedTables().stream()
                .map(TableMetaDataMapper::toDto)
                .collect(Collectors.toList());

        List<TableMetaDataDto> deletedTables = update.getDeletedTables().stream()
                .map(TableMetaDataMapper::toDto)
                .collect(Collectors.toList());

        List<SchemaUpdateDto.Comparison> changedTables = update.getChangedTables().stream()
                .map(comparison ->
                {
                    TableMetaDataDto before = TableMetaDataMapper.toDto(comparison.getBefore());
                    TableMetaDataDto after = TableMetaDataMapper.toDto(comparison.getAfter());

                    return new SchemaUpdateDto.Comparison(before, after);
                })
                .collect(Collectors.toList());

        return new SchemaUpdateDto(update.getUntouchedTables(), addedTables, deletedTables, changedTables);
    }
}
