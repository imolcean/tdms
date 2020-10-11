package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.sql.Time;

@Log
public class TimeGenerationMethod implements PrimitiveGenerationMethod<Time>
{
    @Override
    public Time generate()
    {
        Time now = new Time(System.currentTimeMillis());

        return generate(now, now);
    }

    public Time generate(Time min, Time max)
    {
        log.fine(String.format("Generating a Time between %s and %s", min, max));

        Long _min = min == null ? Time.valueOf("00:00:00").getTime() : min.getTime();
        Long _max = max == null ? Time.valueOf("23:59:59").getTime() : max.getTime();

        long val = new LongGenerationMethod().generate(_min, _max);

        return new Time(val);
    }
}
