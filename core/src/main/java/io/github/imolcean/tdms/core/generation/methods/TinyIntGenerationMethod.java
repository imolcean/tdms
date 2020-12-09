package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.interfaces.generation.method.NumberGenerationMethod;

public class TinyIntGenerationMethod implements NumberGenerationMethod<Short>
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
