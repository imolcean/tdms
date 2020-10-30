package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.interfaces.generation.method.NumberGenerationMethod;
import lombok.extern.java.Log;

import java.util.concurrent.ThreadLocalRandom;

@Log
public class DoubleGenerationMethod implements NumberGenerationMethod<Double>
{
    public Double generate(Number min, Number max)
    {
        min = min == null ? Double.MIN_VALUE : min.doubleValue();
        max = max == null ? Double.MAX_VALUE : max.doubleValue();

        return generate((double) min, (double) max);
    }

    private double generate(double min, double max)
    {
        log.fine(String.format("Generating a Double between %s and %s", min, max));

        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
