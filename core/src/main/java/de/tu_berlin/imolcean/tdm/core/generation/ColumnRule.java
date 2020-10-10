package de.tu_berlin.imolcean.tdm.core.generation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import schemacrawler.schema.Column;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class ColumnRule
{
    private Column column;
    private Object generator; // TODO Generator interface
    private boolean uniqueValues;
    private double nullPart; // TODO JavaDoc Has to be between 0 and 1
    private Map<String, Object> params;

    // TODO Keep List of Columns that this Column depends on (intratable)

    public ColumnRule(Column column, Object generator, boolean uniqueValues, double nullPart, Map<String, Object> params)
    {
        this.column = column;
        this.generator = generator;
        this.uniqueValues = uniqueValues;
        this.nullPart = nullPart;
        this.params = params;

        if(!column.isNullable())
        {
            this.nullPart = 0;
        }

        // TODO If UNIQUE constraint is true, set uniqueValues = true
    }

    public ColumnRule(Column column, Object generator, boolean uniqueValues, double nullPart)
    {
        this(column, generator, uniqueValues, nullPart, new HashMap<>());
    }

    public ColumnRule(Column column, Object generator, Map<String, Object> params)
    {
        this(column, generator, false, 0, params);
    }

    public ColumnRule(Column column, Object generator)
    {
        this(column, generator, false, 0);
    }
}
