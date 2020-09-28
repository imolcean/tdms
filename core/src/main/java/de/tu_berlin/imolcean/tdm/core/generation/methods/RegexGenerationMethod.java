package de.tu_berlin.imolcean.tdm.core.generation.methods;

import com.github.curiousoddman.rgxgen.RgxGen;
import lombok.extern.java.Log;

@Log
public class RegexGenerationMethod
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
}
