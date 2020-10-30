package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRuleDto
{
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnRuleDto
    {
        private String columnName;
        private String generationMethodName;
        private boolean uniqueValues;
        private double nullPart;
        private Map<String, Object> params;
    }

    public enum FillMode
    {
        APPEND,
        UPDATE
    }

    private String tableName;
    private FillMode fillMode;
    private int rowCount;
    // TODO rowCount min, max
    private List<ColumnRuleDto> columnRules;
}
