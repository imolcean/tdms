package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;

import java.util.List;
import java.util.Map;

public interface NumberGenerationMethod<T extends Number> extends PrimitiveGenerationMethod<T>
{
    T generate(Number min, Number max);

    @Override
    default T generate()
    {
        return generate((Number) null, null);
    }

    @Override
    default T generate(Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return generate((Number) args.get(0), (Number) args.get(1));
    }

    @Override
    default List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("min", Number.class, false),
                new GenerationMethodParamDescription("max", Number.class, false));
    }
}
