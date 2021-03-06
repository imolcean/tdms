package io.github.imolcean.tdms.api.interfaces.generation.method;

import io.github.imolcean.tdms.api.GenerationMethodParamDescription;

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
