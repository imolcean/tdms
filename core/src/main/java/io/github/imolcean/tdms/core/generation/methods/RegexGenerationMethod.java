package io.github.imolcean.tdms.core.generation.methods;

import com.github.curiousoddman.rgxgen.RgxGen;
import io.github.imolcean.tdms.api.interfaces.generation.method.GenerationMethod;
import io.github.imolcean.tdms.api.GenerationMethodParamDescription;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Map;

@Log
public class RegexGenerationMethod implements GenerationMethod
{
    public String generate(String pattern)
    {
        if(pattern.isBlank())
        {
            throw new IllegalArgumentException("RegExp pattern cannot be empty");
        }

        log.fine(String.format("Generating a RegExp-based String: %s", pattern));

        return new RgxGen(pattern).generate();
    }

    @Override
    public Object generate(Map<String, Object> params)
    {
        List<Object> args = parseParams(params);

        return generate((String) args.get(0));
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("pattern", String.class, true));
    }
}
