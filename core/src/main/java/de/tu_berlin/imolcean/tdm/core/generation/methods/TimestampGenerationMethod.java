package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.sql.Timestamp;

@Log
public class TimestampGenerationMethod implements PrimitiveGenerationMethod<Timestamp>
{
    @Override
    public Timestamp generate()
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        return generate(now, now);
    }

    public Timestamp generate(Timestamp min, Timestamp max)
    {
        log.fine(String.format("Generating a Timestamp between %s and %s", min, max));

        Long _min = min == null ? Timestamp.valueOf("0001-01-01 00:00:00").getTime() : min.getTime();
        Long _max = max == null ? Timestamp.valueOf("3000-01-01 00:00:00").getTime() : max.getTime();

        long val = new LongGenerationMethod().generate(_min, _max);

        return new Timestamp(val);
    }
}
