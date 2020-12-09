package io.github.imolcean.tdms.api.dto;

import io.github.imolcean.tdms.api.annotations.TsOptional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
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

    private int rowCountTotalOrMin;

    @Getter(onMethod_ = {@TsOptional})
    private int rowCountMax;

    private List<ColumnRuleDto> columnRules;
}
