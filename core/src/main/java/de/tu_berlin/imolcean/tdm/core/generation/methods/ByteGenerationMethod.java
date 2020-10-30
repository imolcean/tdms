package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.interfaces.generation.method.NumberGenerationMethod;
import lombok.extern.java.Log;

import java.util.concurrent.ThreadLocalRandom;

@Log
public class ByteGenerationMethod implements NumberGenerationMethod<Byte>
{
    public Byte generate(Number min, Number max)
    {
        if(min == null || min.longValue() < Byte.MIN_VALUE || min.longValue() > Byte.MAX_VALUE)
        {
            min = Byte.MIN_VALUE;
        }

        if(max == null || max.longValue() > Byte.MAX_VALUE || max.longValue() < Byte.MIN_VALUE)
        {
            max = Byte.MAX_VALUE;
        }

        return generate(min.byteValue(), max.byteValue());
    }

    private byte generate(byte min, byte max)
    {
        log.fine(String.format("Generating a Byte between %s and %s", min, max));

        return (byte) ThreadLocalRandom.current().nextInt(min, max);
    }
}
