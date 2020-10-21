package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.api.TableContent;
import de.tu_berlin.imolcean.tdm.api.exceptions.DataGenerationException;
import de.tu_berlin.imolcean.tdm.core.generation.methods.IntegerGenerationMethod;
import lombok.Data;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Log
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
    private Map<Column, ColumnRule> columnRules;

    public TableRule(Table table, FillMode fillMode, int rowCount)
    {
        this.table = table;
        this.fillMode = fillMode;
        this.rowCount = rowCount;
        this.columnRules = new TreeMap<>();
    }

    public TableRule(Table table, FillMode fillMode, int minRowCount, int maxRowCount)
    {
        this(table, fillMode, new IntegerGenerationMethod().generate(minRowCount, maxRowCount));
    }

    public boolean isPostponed()
    {
        return getPostponedColumnRules().size() > 0;
    }

    public boolean isValid()
    {
        // TODO Check that columnRules only operate on Columns of this Table (no foreign Columns)

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
        return CollectionUtils.disjunction(getMandatoryColumns(), CollectionUtils.intersection(getMandatoryColumns(), columnRules.keySet()));
    }

    public List<ColumnRule> getOrderedColumnRules()
    {
        // TODO Order according to intratabular dependencies

        return new ArrayList<>(columnRules.values());
    }

    public List<ColumnRule> getPostponedColumnRules()
    {
        return columnRules.values().stream()
                .filter(ColumnRule::isPostponed)
                .collect(Collectors.toList());
    }

    public void setColumnRules(Collection<ColumnRule> rules)
    {
        columnRules.clear();
        rules.forEach(rule -> columnRules.put(rule.getColumn(), rule));
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

    public void generate(TableContent existingContent)
    {
        if(fillMode.equals(FillMode.APPEND))
        {
            generateAppend(existingContent);
        }
        else
        {
            generateUpdate(existingContent);
        }
    }

    private void generateUpdate(TableContent content)
    {
        if(!isValid())
        {
            throw new DataGenerationException(String.format("Cannot generate data for table %s because the table rule is invalid", table.getName()));
        }

        log.info("Generating data for table " + table.getName() + " in UPDATE fill mode");
        for(int i = 0; i < content.getRows().size(); i++)
        {
            log.fine(String.format("Generating row %s/%s", i, content.getRows().size()));
            for(ColumnRule cr : getOrderedColumnRules())
            {
                if(cr.getColumn().isPartOfPrimaryKey())
                {
                    // TODO Optional: Require UPDATE of the referencing Columns in other tables
                    log.warning(String.format("Cannot update values in column '%s' because it is part of the primary key", cr.getColumn().getName()));
                    continue;
                }

                List<Object> existingColumnContent = content.getRows().stream()
                        .map(r -> r.getValue(cr.getColumn()))
                        .collect(Collectors.toList());

                log.fine("Generating value for column " + cr.getColumn().getName());
                Object value = cr.generate(existingColumnContent);
                log.fine("Value: " + value);

                content.getRow(i).setValue(cr.getColumn(), value);
            }
        }
    }

    private void generateAppend(TableContent content)
    {
        if(!isValid())
        {
            throw new DataGenerationException(String.format("Cannot generate data for table %s because the table rule is invalid", table.getName()));
        }

        log.info("Generating data for table " + table.getName() + " in APPEND fill mode");

        for(int i = 0; i < rowCount; i++)
        {
            log.fine(String.format("Generating row %s/%s", i, rowCount));
            TableContent.Row row = new TableContent.Row(table);

            for(ColumnRule cr : getOrderedColumnRules())
            {
                List<Object> existingColumnContent = content.getRows().stream()
                        .map(r -> r.getValue(cr.getColumn()))
                        .collect(Collectors.toList());

                log.fine("Generating value for column " + cr.getColumn().getName());
                Object value = cr.generate(existingColumnContent);
                log.fine("Value: " + value);

                row.setValue(cr.getColumn(), value);
            }

            content.addRow(row);
        }
    }
}
