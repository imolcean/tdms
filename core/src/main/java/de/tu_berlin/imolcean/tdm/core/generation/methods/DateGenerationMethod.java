package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.sql.Date;

@Log
public class DateGenerationMethod implements PrimitiveGenerationMethod<Date>
{
    @Override
    public Date generate()
    {
        Date now = new Date(System.currentTimeMillis());

        return generate(now, now);
    }

    public Date generate(Date min, Date max)
    {
        log.fine(String.format("Generating a Date between %s and %s", min, max));

        Long _min = min == null ? Date.valueOf("0001-01-01").getTime() : min.getTime();
        Long _max = max == null ? Date.valueOf("3000-01-01").getTime() : max.getTime();

        long val = new LongGenerationMethod().generate(_min, _max);

        return new Date(val);
    }
}
