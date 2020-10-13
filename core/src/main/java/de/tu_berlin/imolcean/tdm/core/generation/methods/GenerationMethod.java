package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import schemacrawler.schema.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GenerationMethod
{
    Object generate(Column column, Map<String, Object>params);

    List<GenerationMethodParamDescription> getParamDescription();

    default List<Object> parseParams(Map<String, Object> params)
    {
        List<Object> args = new ArrayList<>();

        for(GenerationMethodParamDescription description : getParamDescription())
        {
            Object val = params.get(description.getName());

            if(description.isRequired() && val == null)
            {
                throw new IllegalArgumentException(String.format("Parameter %s is not provided", description.getName()));
            }

            if(val != null && !description.getType().isInstance(val))
            {
                throw new IllegalArgumentException(String.format("Parameter %s is not of type %s", description.getName(), description.getType()));
            }

            args.add(val);
        }

        return args;
    }
}
