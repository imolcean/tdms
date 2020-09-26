package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.util.concurrent.ThreadLocalRandom;

@Log
public class RandLongGenerationMethod implements RandNumberGenerationMethod<Long>
{
    public Long generate(Number min, Number max)
    {
        min = min == null ? Long.MIN_VALUE : min.longValue();
        max = max == null ? Long.MAX_VALUE : max.longValue();

        return generate((long) min, (long) max);
    }

    private long generate(long min, long max)
    {
        log.fine(String.format("Generating an Long between %s and %s", min, max));

        return ThreadLocalRandom.current().nextLong(min, max);
    }
}
