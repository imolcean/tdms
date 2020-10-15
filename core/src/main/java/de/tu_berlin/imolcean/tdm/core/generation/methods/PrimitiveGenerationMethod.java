package de.tu_berlin.imolcean.tdm.core.generation.methods;

import java.util.Map;

public interface PrimitiveGenerationMethod<T> extends GenerationMethod
{
    T generate();

    @Override
    T generate(Map<String, Object> params);
}
