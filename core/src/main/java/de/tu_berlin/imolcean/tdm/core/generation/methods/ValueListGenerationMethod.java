package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.ValueLibrary;
import de.tu_berlin.imolcean.tdm.api.exceptions.DataGenerationException;
import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Map;

@Log
public class ValueListGenerationMethod implements GenerationMethod
{
    public Object pick(ValueLibrary lib)
    {
        log.fine("Picking from a value library " + lib.getId());

        if(!lib.isList())
        {
            throw new DataGenerationException(String.format("The value library %s is not a value list", lib.getId()));
        }

        return pick(lib.getList().toArray());
    }

    public Object pick(Object[] options)
    {
        log.fine(String.format("Picking from a list of %s elements", options == null ? null : options.length));

        if(options == null || options.length == 0)
        {
            return null;
        }

        int randomIndex = new IntegerGenerationMethod().generate(0, options.length);

        return options[randomIndex];
    }

    @Override
    public Object generate(Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return pick((Object[]) args.get(0));
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("options", Object[].class, true));
    }
}
