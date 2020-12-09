package io.github.imolcean.tdms.core.controllers.mappers;

import io.github.imolcean.tdms.api.dto.SchemaUpdateDto;
import io.github.imolcean.tdms.api.dto.TableMetaDataDto;
import io.github.imolcean.tdms.api.interfaces.updater.SchemaUpdater;

import java.util.List;
import java.util.stream.Collectors;

public class SchemaUpdateMapper
{
    public static SchemaUpdateDto toDto(SchemaUpdater.SchemaUpdateReport report)
    {
        List<TableMetaDataDto> addedTables = report.getAddedTables().stream()
                .map(TableMetaDataMapper::toDto)
                .collect(Collectors.toList());

        List<TableMetaDataDto> deletedTables = report.getDeletedTables().stream()
                .map(TableMetaDataMapper::toDto)
                .collect(Collectors.toList());

        List<SchemaUpdateDto.Comparison> changedTables = report.getChangedTables().stream()
                .map(comparison ->
                {
                    TableMetaDataDto before = TableMetaDataMapper.toDto(comparison.getBefore());
                    TableMetaDataDto after = TableMetaDataMapper.toDto(comparison.getAfter());

                    return new SchemaUpdateDto.Comparison(before, after);
                })
                .collect(Collectors.toList());

        return new SchemaUpdateDto(report.getUntouchedTables(), addedTables, deletedTables, changedTables);
    }
}
