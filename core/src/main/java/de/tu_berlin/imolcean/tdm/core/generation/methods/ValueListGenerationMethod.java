package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.util.List;
import java.util.Map;

@Log
public class ValueListGenerationMethod implements GenerationMethod
{
    public Object pick(Object[] arr)
    {
        log.fine(String.format("Picking from a list of %s elements", arr == null ? null : arr.length));

        if(arr == null || arr.length == 0)
        {
            return null;
        }

        int randomIndex = new IntegerGenerationMethod().generate(0, arr.length);

        return arr[randomIndex];
    }

    @Override
    public Object generate(Column column, Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return pick((Object[]) args.get(0));
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("min", Object[].class, false));
    }
}
