package io.github.imolcean.tdms.core.generation;

import io.github.imolcean.tdms.api.TableContent;
import io.github.imolcean.tdms.api.exceptions.DataGenerationException;
import io.github.imolcean.tdms.core.generation.methods.FormulaGenerationMethod;
import io.github.imolcean.tdms.api.interfaces.generation.method.GenerationMethod;
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
        this.postponed = false;
        this.nullPart = !column.isNullable() ? 0 : nullPart;
        this.uniqueValues = this.column.isPartOfPrimaryKey() || this.column.isPartOfUniqueIndex() || uniqueValues;
        this.params = params != null ? params : new HashMap<>();

        if(this.generationMethod instanceof FormulaGenerationMethod)
        {
            this.dependencies = ((FormulaGenerationMethod) this.generationMethod).findDependencies(params);
        }
        else
        {
            this.dependencies = new HashSet<>();
        }
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
