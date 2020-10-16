package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.core.generation.methods.IntegerGenerationMethod;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class TableRule
{
    public enum FillMode
    {
        APPEND,
        UPDATE
    }

    private Table table;
    private FillMode fillMode;
    private int rowCount;

    @Getter(AccessLevel.NONE)
    private Map<Column, ColumnRule> columnRules;

    public TableRule(Table table, FillMode fillMode, int rowCount)
    {
        this.table = table;
        this.fillMode = fillMode;
        this.rowCount = rowCount;
    }

    public TableRule(Table table, FillMode fillMode, int minRowCount, int maxRowCount)
    {
        this(table, fillMode, new IntegerGenerationMethod().generate(minRowCount, maxRowCount));
    }

    public boolean isValid()
    {
        if(fillMode == FillMode.UPDATE)
        {
            return true;
        }

        return getMandatoryColumnsWithoutRules().size() == 0;
    }

    // Columns that are non-null and have no default
    public Collection<Column> getMandatoryColumns()
    {
        if(fillMode == FillMode.UPDATE)
        {
            return Collections.emptyList();
        }

        return table.getColumns().stream()
                .filter(column -> !column.isNullable() && !column.hasDefaultValue())
                .collect(Collectors.toSet());
    }

    public Collection<Column> getMandatoryColumnsWithoutRules()
    {
        return CollectionUtils.disjunction(getMandatoryColumns(), columnRules.keySet());
    }

    public List<ColumnRule> getOrderedColumnRules()
    {
        // TODO Order according to intratabular dependencies

        return new ArrayList<>(columnRules.values());
    }

    public Optional<ColumnRule> findColumnRule(Column column)
    {
        return Optional.ofNullable(columnRules.get(column));
    }

    public boolean hasColumnRule(Column column)
    {
        return columnRules.containsKey(column);
    }

    public void setColumnRule(ColumnRule columnRule)
    {
        columnRules.put(columnRule.getColumn(), columnRule);
    }

    public void clearColumnRule(Column column)
    {
        columnRules.remove(column);
    }
}
