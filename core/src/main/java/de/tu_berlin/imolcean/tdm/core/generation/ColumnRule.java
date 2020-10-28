package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.api.TableContent;
import de.tu_berlin.imolcean.tdm.api.exceptions.DataGenerationException;
import de.tu_berlin.imolcean.tdm.core.generation.methods.FormulaGenerationMethod;
import de.tu_berlin.imolcean.tdm.core.generation.methods.GenerationMethod;
import lombok.*;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.util.*;

@Data
@Log
public class ColumnRule
{
    private final TableRule parent;
    private final Column column;
    private final GenerationMethod generationMethod;
    private final boolean uniqueValues;
    private final double nullPart; // TODO JavaDoc: Has to be between 0 and 1
    private final Map<String, Object> params;
    private final Set<Column> dependencies;

    private boolean postponed;

    public ColumnRule(TableRule parent,
                      Column column,
                      GenerationMethod generationMethod,
                      boolean uniqueValues,
                      double nullPart,
                      Map<String, Object> params)
    {
        this.parent = parent;
        this.column = column;
        this.generationMethod = generationMethod;
        this.params = params;
        this.postponed = false;
        this.nullPart = !column.isNullable() ? 0 : nullPart;
        this.uniqueValues = this.column.isPartOfPrimaryKey() || this.column.isPartOfUniqueIndex() || uniqueValues;

        if(this.generationMethod instanceof FormulaGenerationMethod)
        {
            this.dependencies = ((FormulaGenerationMethod) this.generationMethod).findDependencies(params);
        }
        else
        {
            this.dependencies = new HashSet<>();
        }
    }

    public ColumnRule(TableRule parent, Column column, GenerationMethod generationMethod, boolean uniqueValues, double nullPart)
    {
        this(parent, column, generationMethod, uniqueValues, nullPart, new HashMap<>());
    }

    public ColumnRule(TableRule parent, Column column, GenerationMethod generationMethod, Map<String, Object> params)
    {
        this(parent, column, generationMethod, false, 0, params);
    }

    public ColumnRule(TableRule parent, Column column, GenerationMethod generationMethod)
    {
        this(parent, column, generationMethod, false, 0);
    }

    public Object generate(List<Object> content, TableContent.Row currentRow)
    {
        if(postponed)
        {
            return null;
        }

        // If uniqueness flag is set and a NULL is already contained in the column, skip generation of another NULL
        if(nullPart > 0 && !(uniqueValues && content.contains(null)))
        {
            if(nullPart >= 1)
            {
                return null;
            }

            if(new Random().nextDouble() < nullPart)
            {
                return null;
            }
        }

        if(generationMethod instanceof FormulaGenerationMethod && !dependencies.isEmpty())
        {
            ((FormulaGenerationMethod) generationMethod).fillPlaceholders(currentRow.getValuesAsMap());
        }

        Object value = generationMethod.generate(params);

        if(uniqueValues)
        {
            // TODO Optional: Determine the exact amount of possible unique values
            int attempt = 0;
            while(content.contains(value))
            {
                if(attempt > 20)
                {
                    throw new DataGenerationException(String.format("Cannot generate another unique value for column '%s'", column.getName()));
                }

                value = generationMethod.generate(params);
                attempt++;
            }
        }

        return value;
    }
}
