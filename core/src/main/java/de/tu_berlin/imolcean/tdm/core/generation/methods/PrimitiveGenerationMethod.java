package de.tu_berlin.imolcean.tdm.core.generation.methods;

import schemacrawler.schema.Column;

import java.util.Map;

public interface PrimitiveGenerationMethod<T> extends GenerationMethod
{
    T generate();

    @Override
    T generate(Column column, Map<String, Object> params);
}
