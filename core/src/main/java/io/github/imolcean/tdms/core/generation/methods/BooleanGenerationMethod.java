package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.interfaces.generation.method.PrimitiveGenerationMethod;
import io.github.imolcean.tdms.api.GenerationMethodParamDescription;
import lombok.extern.java.Log;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Log
public class BooleanGenerationMethod implements PrimitiveGenerationMethod<Boolean>
{
    @Override
    public Boolean generate()
    {
        log.fine("Generating a Boolean");

        return ThreadLocalRandom.current().nextBoolean();
    }

    @Override
    public Boolean generate(Map<String, Object> params)
    {
        return generate();
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return Collections.emptyList();
    }
}
