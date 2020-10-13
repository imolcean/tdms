package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Log
public class DateGenerationMethod implements PrimitiveGenerationMethod<Date>
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
    public Date generate(Column column, Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return generate(Date.valueOf((String) args.get(0)), Date.valueOf((String) args.get(1)));
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("min", String.class, false),
                new GenerationMethodParamDescription("max", String.class, false));
    }
}
