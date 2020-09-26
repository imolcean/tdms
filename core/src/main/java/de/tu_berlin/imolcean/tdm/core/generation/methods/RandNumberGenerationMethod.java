package de.tu_berlin.imolcean.tdm.core.generation.methods;

public interface RandNumberGenerationMethod<T extends Number>
{
    T generate(Number min, Number max);
}
