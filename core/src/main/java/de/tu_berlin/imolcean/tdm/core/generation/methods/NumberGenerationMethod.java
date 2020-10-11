package de.tu_berlin.imolcean.tdm.core.generation.methods;

public interface NumberGenerationMethod<T extends Number> extends PrimitiveGenerationMethod<T>
{
    T generate(Number min, Number max);

    default T generate()
    {
        return generate(null, null);
    }
}
