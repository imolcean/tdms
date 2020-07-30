package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaUpdateDataMappingRequest
{
    @Data
    @AllArgsConstructor
    public static class TableDataMigrationRequest
    {
        String tableName;
        String sql;
    }

    List<TableDataMigrationRequest> sqlMigrationTables;
}
