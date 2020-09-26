package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.util.concurrent.ThreadLocalRandom;

@Log
public class RandShortGenerationMethod implements RandNumberGenerationMethod<Short>
{
    public Short generate(Number min, Number max)
    {
        if(min == null || min.longValue() < Short.MIN_VALUE || min.longValue() > Short.MAX_VALUE)
        {
            min = Short.MIN_VALUE;
        }

        if(max == null || max.longValue() > Short.MAX_VALUE || max.longValue() < Short.MIN_VALUE)
        {
            max = Short.MAX_VALUE;
        }

        return generate(min.shortValue(), max.shortValue());
    }

    private short generate(short min, short max)
    {
        log.fine(String.format("Generating a Short between %s and %s", min, max));

        return (short) ThreadLocalRandom.current().nextInt(min, max);
    }
}
