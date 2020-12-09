package io.github.imolcean.tdms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SchemaUpdateDto
{
    @Data
    @AllArgsConstructor
    public static class Comparison
    {
        TableMetaDataDto before;
        TableMetaDataDto after;
    }

    List<String> untouchedTables;
    List<TableMetaDataDto> addedTables;
    List<TableMetaDataDto> deletedTables;
    List<Comparison> changedTables;
}
