package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.exceptions.DataGenerationException;
import de.tu_berlin.imolcean.tdm.core.generation.FormulaService;
import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import javax.script.ScriptException;
import java.util.List;
import java.util.Map;

@Log
public class FormulaGenerationMethod implements GenerationMethod, ColumnAwareGenerationMethod
{
    private final FormulaService formulaService;
    private final Column column;

    public FormulaGenerationMethod(FormulaService formulaService, Column column)
    {
        this.formulaService = formulaService;
        this.column = column;
    }

    @Override
    public Object generate(Map<String, Object> params)
    {
        log.fine("Generating a value using a formula");

        List<Object> args = parseParams(params);
        Object val;

        try
        {
            val = formulaService.createEngine(column).eval((String) args.get(0));
        }
        catch(ScriptException e)
        {
            throw new DataGenerationException(e);
        }

        Class<?> columnType = column.getColumnDataType().getTypeMappedClass();

        if(!columnType.isAssignableFrom(val.getClass()))
        {
            throw new DataGenerationException(
                    String.format(
                            "Value generated by the formula evaluation for column '%s' has type %s, %s required",
                            column.getFullName(),
                            val.getClass(),
                            columnType));
        }

        return val;
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("formula", String.class, true));
    }
}
