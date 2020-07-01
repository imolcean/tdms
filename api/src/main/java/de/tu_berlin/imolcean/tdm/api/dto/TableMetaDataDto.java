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

        private boolean primaryKey;

        // TODO FK
    }

    private String name;

    private List<Column> columns;

    // TODO PK

    // TODO FK
}
