package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.interfaces.generation.method.NumberGenerationMethod;
import lombok.extern.java.Log;

import java.util.concurrent.ThreadLocalRandom;

@Log
public class LongGenerationMethod implements NumberGenerationMethod<Long>
{
    public Long generate(Number min, Number max)
    {
        min = min == null ? Long.MIN_VALUE : min.longValue();
        max = max == null ? Long.MAX_VALUE : max.longValue();

        return generate((long) min, (long) max);
    }

    private long generate(long min, long max)
    {
        log.fine(String.format("Generating a Long between %s and %s", min, max));

        return ThreadLocalRandom.current().nextLong(min, max);
    }
}
