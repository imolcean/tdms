package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.ValueLibrary;
import io.github.imolcean.tdms.api.exceptions.DataGenerationException;
import io.github.imolcean.tdms.api.interfaces.generation.method.GenerationMethod;
import io.github.imolcean.tdms.api.GenerationMethodParamDescription;
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

        return pick(lib.getList());
    }

    public Object pick(List<?> options)
    {
        log.fine(String.format("Picking from a list of %s elements", options == null ? null : options.size()));

        if(options == null || options.size() == 0)
        {
            return null;
        }

        int randomIndex = new IntegerGenerationMethod().generate(0, options.size());

        return options.get(randomIndex);
    }

    @Override
    public Object generate(Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return pick((List<?>) args.get(0));
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("options", List.class, true));
    }
}
