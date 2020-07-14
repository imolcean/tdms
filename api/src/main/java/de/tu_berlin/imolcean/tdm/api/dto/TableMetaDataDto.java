package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TableMetaDataDto
{
    @Data
    @AllArgsConstructor
    public static class Column
    {
        private String name;
        private String type;
        private boolean nullable;
    }

    @Data
    @AllArgsConstructor
    public static class PrimaryKey
    {
        private String name;
        private List<String> columnNames;
    }

    @Data
    @AllArgsConstructor
    public static class ForeignKey
    {
        private String name;
        private List<String> columnNames;
        private String pkTableName;
        private List<String> pkColumnNames;
    }

    private String name;
    private List<Column> columns;
    private PrimaryKey pk;
    private List<ForeignKey> fks;
}
