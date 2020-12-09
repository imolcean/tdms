package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.interfaces.generation.method.NumberGenerationMethod;
import lombok.extern.java.Log;

import java.util.concurrent.ThreadLocalRandom;

@Log
public class IntegerGenerationMethod implements NumberGenerationMethod<Integer>
{
    public Integer generate(Number min, Number max)
    {
        if(min == null || min.longValue() < Integer.MIN_VALUE || min.longValue() > Integer.MAX_VALUE)
        {
            min = Integer.MIN_VALUE;
        }

        if(max == null || max.longValue() > Integer.MAX_VALUE || max.longValue() < Integer.MIN_VALUE)
        {
            max = Integer.MAX_VALUE;
        }

        return generate(min.intValue(), max.intValue());
    }

    private int generate(int min, int max)
    {
        log.fine(String.format("Generating an Integer between %s and %s", min, max));

        return ThreadLocalRandom.current().nextInt(min, max);
    }
}
