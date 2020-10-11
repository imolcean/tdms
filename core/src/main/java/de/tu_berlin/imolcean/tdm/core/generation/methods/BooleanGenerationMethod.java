package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

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
}
