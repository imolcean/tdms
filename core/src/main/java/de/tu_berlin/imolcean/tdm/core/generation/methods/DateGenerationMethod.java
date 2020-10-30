package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.interfaces.generation.method.TimelineGenerationMethod;
import lombok.extern.java.Log;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Log
public class DateGenerationMethod implements TimelineGenerationMethod<Date>
{
    public Date generate(Date min, Date max)
    {
        log.fine(String.format("Generating a Date between %s and %s", min, max));

        Long _min = min == null ? Date.valueOf("0001-01-01").getTime() : min.getTime();
        Long _max = max == null ? Date.valueOf("3000-01-01").getTime() : max.getTime();

        long val = new LongGenerationMethod().generate(_min, _max);

        return new Date(val);
    }

    @Override
    public Date generate()
    {
        Date now = new Date(System.currentTimeMillis());

        return generate(now, now);
    }

    @Override
    public Date generate(Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return generate(
                args.get(0) != null ? Date.valueOf((String) args.get(0)) : null,
                args.get(1) != null ? Date.valueOf((String) args.get(1)) : null);
    }
}
