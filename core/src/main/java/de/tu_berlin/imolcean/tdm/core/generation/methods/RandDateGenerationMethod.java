package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.sql.Date;

@Log
public class RandDateGenerationMethod
{
    public Date generate(Date min, Date max)
    {
        log.fine(String.format("Generating Date between %s and %s", min, max));

        Long _min = min == null ? Date.valueOf("0001-01-01").getTime() : min.getTime();
        Long _max = max == null ? null : max.getTime();

        long val = new RandLongGenerationMethod().generate(_min, _max);

        return new Date(val);
    }
}
