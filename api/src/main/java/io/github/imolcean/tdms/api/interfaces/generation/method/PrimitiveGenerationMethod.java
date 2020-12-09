package io.github.imolcean.tdms.api.interfaces.generation.method;

import java.util.Map;

public interface PrimitiveGenerationMethod<T> extends GenerationMethod
{
    T generate();

    @Override
    T generate(Map<String, Object> params);
}
