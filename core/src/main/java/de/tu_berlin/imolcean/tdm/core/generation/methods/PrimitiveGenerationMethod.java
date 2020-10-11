package de.tu_berlin.imolcean.tdm.core.generation.methods;

public interface PrimitiveGenerationMethod<T> extends GenerationMethod
{
    T generate();
}
