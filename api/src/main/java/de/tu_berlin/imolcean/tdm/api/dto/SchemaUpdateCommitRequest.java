package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SchemaUpdateCommitRequest
{
    @Data
    @AllArgsConstructor
    public static class TableDataMigrationRequest
    {
        String tableName;
        String sql;
    }

    List<String> autoMigrationTables;
    List<TableDataMigrationRequest> sqlMigrationTables;
}
