package de.tu_berlin.imolcean.tdm.core.generation.methods;

public class TinyintGenerationMethod implements NumberGenerationMethod<Short>
{
    @Override
    public Short generate(Number min, Number max)
    {
        if(min == null || min.intValue() < 0 || min.intValue() > 256)
        {
            min = 0;
        }

        if(max == null || max.intValue() < 0 || max.intValue() > 256)
        {
            max = 256;
        }

        return new ShortGenerationMethod().generate(min, max);
    }
}
