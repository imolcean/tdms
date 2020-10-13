package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

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
    public Boolean generate(Column column, Map<String, Object> params)
    {
        return generate();
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return Collections.emptyList();
    }
}
