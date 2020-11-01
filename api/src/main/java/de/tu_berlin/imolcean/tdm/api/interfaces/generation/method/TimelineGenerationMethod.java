package de.tu_berlin.imolcean.tdm.api.interfaces.generation.method;

import de.tu_berlin.imolcean.tdm.api.GenerationMethodParamDescription;

import java.util.Date;
import java.util.List;

public interface TimelineGenerationMethod<T extends Date> extends PrimitiveGenerationMethod<T>
{
    T generate(T min, T max);

    @Override
    default List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("min", String.class, false),
                new GenerationMethodParamDescription("max", String.class, false));
    }
}