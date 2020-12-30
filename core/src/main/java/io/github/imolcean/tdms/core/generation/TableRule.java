package io.github.imolcean.tdms.core.generation;

import io.github.imolcean.tdms.api.TableContent;
import io.github.imolcean.tdms.api.exceptions.DataGenerationException;
import io.github.imolcean.tdms.core.generation.methods.IntegerGenerationMethod;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObject;
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

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<ColumnRule> orderCache;

    public TableRule(Table table, FillMode fillMode, int rowCount)
    {
        this.table = table;
        this.fillMode = fillMode;
        this.rowCount = rowCount;
        this.columnRules = new TreeMap<>();
        this.orderCache = new ArrayList<>();
    }

    public TableRule(Table table, FillMode fillMode, int minRowCount, int maxRowCount)
    {
        this(
                table,
                fillMode,
                maxRowCount == 0
                        ? minRowCount
                        : new IntegerGenerationMethod().generate(minRowCount, maxRowCount));
    }

    public boolean isPostponed()
    {
        return getOrderedPostponedColumnRules().size() > 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid()
    {
        // Check that columnRules only contains Columns of this Table (no foreign Columns)
        Collection<Column> foreignColumns = columnRules.keySet().stream()
                .filter(column -> !column.getParent().equals(table))
                .collect(Collectors.toList());

        if(foreignColumns.size() > 0)
        {
            log.warning(
                    String.format(
                            "The table '%s' has specified rules for the following foreign columns: %s",
                            table.getName(),
                            foreignColumns.stream()
                                    .map(NamedObject::getName)
                                    .collect(Collectors.joining(", "))));

            return false;
        }

        // If FillMode is UPDATE, mandatory columns don't matter
        if(fillMode == FillMode.UPDATE)
        {
            return true;
        }

        // If FillMode is APPEND, mandatory columns must have rules specified
        Collection<Column> mandatoryColumnsWithoutRules = getMandatoryColumnsWithoutRules();
        if(mandatoryColumnsWithoutRules.size() > 0)
        {
            log.warning(
                    String.format(
                            "The table '%s' has the following mandatory columns that have no rules specified: %s",
                            table.getName(),
                            mandatoryColumnsWithoutRules.stream()
                                    .map(NamedObject::getName)
                                    .collect(Collectors.joining(", "))));

            return false;
        }

        return true;
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
        if(!orderCache.isEmpty())
        {
            return orderCache;
        }

        DefaultDirectedGraph<Column, DefaultEdge> graph = new DependencyGraphCreator().createForColumns(this);

        if(new CycleDetector<>(graph).detectCycles())
        {
            throw new DataGenerationException(String.format("Column rules for table %s contain circular intratable dependencies", table.getName()));
        }

        new TopologicalOrderIterator<>(graph)
                .forEachRemaining(column ->
                {
                    if(columnRules.get(column) != null)
                    {
                        orderCache.add(columnRules.get(column));
                    }
                });

        return orderCache;
    }

    public List<ColumnRule> getOrderedPostponedColumnRules()
    {
        return getOrderedColumnRules().stream()
                .filter(ColumnRule::isPostponed)
                .collect(Collectors.toList());
    }

    public Optional<ColumnRule> findColumnRule(Column column)
    {
        return Optional.ofNullable(columnRules.get(column));
    }

    public boolean hasColumnRule(Column column)
    {
        return columnRules.containsKey(column);
    }

    public void setColumnRules(Collection<ColumnRule> rules)
    {
        columnRules.clear();
        orderCache.clear();
        rules.forEach(rule -> columnRules.put(rule.getColumn(), rule));
    }

    public void putColumnRule(ColumnRule columnRule)
    {
        orderCache.clear();
        columnRules.put(columnRule.getColumn(), columnRule);
    }

    public void clearColumnRule(Column column)
    {
        orderCache.clear();
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
            log.fine(String.format("Generating row %s/%s", i + 1, content.getRows().size()));
            for(ColumnRule cr : getOrderedColumnRules())
            {
                if(cr.getColumn().isPartOfPrimaryKey())
                {
                    // TODO Optional: Require UPDATE of the referencing Columns in other tables
                    log.warning(String.format("Cannot update values in column '%s' because it is part of the primary key", cr.getColumn().getName()));
                    continue;
                }

                List<Object> existingColumnContent = content.getRows().stream()
                        .map(_row -> _row.getValue(cr.getColumn()))
                        .collect(Collectors.toList());

                log.fine("Generating value for column " + cr.getColumn().getName());
                Object value = cr.generate(existingColumnContent, content.getRow(i));
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
            log.fine(String.format("Generating row %s/%s", i + 1, rowCount));
            TableContent.Row row = new TableContent.Row(table);

            for(ColumnRule cr : getOrderedColumnRules())
            {
                List<Object> existingColumnContent = content.getRows().stream()
                        .map(r -> r.getValue(cr.getColumn()))
                        .collect(Collectors.toList());

                log.fine("Generating value for column " + cr.getColumn().getName());
                Object value = cr.generate(existingColumnContent, row);
                log.fine("Value: " + value);

                row.setValue(cr.getColumn(), value);
            }

            content.addRow(row);
        }
    }
}
