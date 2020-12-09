package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.interfaces.generation.method.TimelineGenerationMethod;
import lombok.extern.java.Log;

import java.sql.Time;
import java.util.List;
import java.util.Map;

@Log
public class TimeGenerationMethod implements TimelineGenerationMethod<Time>
{
    public Time generate(Time min, Time max)
    {
        log.fine(String.format("Generating a Time between %s and %s", min, max));

        Long _min = min == null ? Time.valueOf("00:00:00").getTime() : min.getTime();
        Long _max = max == null ? Time.valueOf("23:59:59").getTime() : max.getTime();

        long val = new LongGenerationMethod().generate(_min, _max);

        return new Time(val);
    }

    @Override
    public Time generate()
    {
        Time now = new Time(System.currentTimeMillis());

        return generate(now, now);
    }

    @Override
    public Time generate(Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return generate(
                args.get(0) != null ? Time.valueOf((String) args.get(0)) : null,
                args.get(1) != null ? Time.valueOf((String) args.get(1)) : null);
    }
}
