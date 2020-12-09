package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.interfaces.generation.method.NumberGenerationMethod;
import lombok.extern.java.Log;

import java.util.concurrent.ThreadLocalRandom;

@Log
public class FloatGenerationMethod implements NumberGenerationMethod<Float>
{
    public Float generate(Number min, Number max)
    {
        if(min == null || min.doubleValue() < -Float.MAX_VALUE || min.doubleValue() > Float.MAX_VALUE)
        {
            min = -Float.MAX_VALUE;
        }

        if(max == null || max.doubleValue() > Float.MAX_VALUE || max.doubleValue() < -Float.MAX_VALUE)
        {
            max = Float.MAX_VALUE;
        }

        return generate(min.floatValue(), max.floatValue());
    }

    private float generate(float min, float max)
    {
        log.fine(String.format("Generating a Float between %s and %s", min, max));

        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }
}
