package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.interfaces.generation.method.TimelineGenerationMethod;
import lombok.extern.java.Log;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Log
public class TimestampGenerationMethod implements TimelineGenerationMethod<Timestamp>
{
    public Timestamp generate(Timestamp min, Timestamp max)
    {
        log.fine(String.format("Generating a Timestamp between %s and %s", min, max));

        Long _min = min == null ? Timestamp.valueOf("1753-01-01 00:00:00").getTime() : min.getTime();
        Long _max = max == null ? Timestamp.valueOf("3000-01-01 00:00:00").getTime() : max.getTime();

        long val = new LongGenerationMethod().generate(_min, _max);

        return new Timestamp(val);
    }

    @Override
    public Timestamp generate()
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        return generate(now, now);
    }

    @Override
    public Timestamp generate(Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return generate(
                args.get(0) != null ? Timestamp.valueOf((String) args.get(0)) : null,
                args.get(1) != null ? Timestamp.valueOf((String) args.get(1)) : null);
    }
}
