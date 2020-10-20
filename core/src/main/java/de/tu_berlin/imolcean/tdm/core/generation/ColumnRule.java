package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.core.generation.methods.GenerationMethod;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
    private GenerationMethod generationMethod;
    private boolean uniqueValues;
    private double nullPart; // TODO JavaDoc: Has to be between 0 and 1
    private Map<String, Object> params;

    @Setter
    private boolean postponed;

    // TODO Keep List of Columns that this Column depends on (intratable dependencies)

    public ColumnRule(Column column,
                      GenerationMethod generationMethod,
                      boolean uniqueValues,
                      double nullPart,
                      Map<String, Object> params)
    {
        this.column = column;
        this.generationMethod = generationMethod;
        this.uniqueValues = uniqueValues;
        this.nullPart = nullPart;
        this.params = params;
        this.postponed = false;

        if(!column.isNullable())
        {
            this.nullPart = 0;
        }

        if(this.column.isPartOfPrimaryKey() || this.column.isPartOfUniqueIndex())
        {
            this.uniqueValues = true;
        }
    }

    public ColumnRule(Column column, GenerationMethod generationMethod, boolean uniqueValues, double nullPart)
    {
        this(column, generationMethod, uniqueValues, nullPart, new HashMap<>());
    }

    public ColumnRule(Column column, GenerationMethod generationMethod, Map<String, Object> params)
    {
        this(column, generationMethod, false, 0, params);
    }

    public ColumnRule(Column column, GenerationMethod generationMethod)
    {
        this(column, generationMethod, false, 0);
    }

    public Object generate()
    {
        if(postponed)
        {
            return null;
        }

        // TODO Handle NullPart and UniqueValues

        return generationMethod.generate(params);
    }
}
