package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.sql.Time;

@Log
public class RandTimeGenerationMethod
{
    public Time generate(Time min, Time max)
    {
        log.fine(String.format("Generating a Time between %s and %s", min, max));

        Long _min = min == null ? Time.valueOf("00:00:00").getTime() : min.getTime();
        Long _max = max == null ? Time.valueOf("23:59:59").getTime() : max.getTime();

        long val = new RandLongGenerationMethod().generate(_min, _max);

        return new Time(val);
    }
}
