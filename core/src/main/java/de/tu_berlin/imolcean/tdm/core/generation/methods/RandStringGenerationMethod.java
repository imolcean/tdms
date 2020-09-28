package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

@Log
public class RandStringGenerationMethod
{
    public enum Capitalization
    {
        LOWER,
        UPPER,
        FIRST_UPPER,
        MIXED,
    }

    public String generate(int minLength, int maxLength, String capitalization)
    {
        Capitalization cap;

        if(capitalization == null)
        {
            capitalization = "MIXED";
        }

        switch(capitalization.toUpperCase())
        {
            case "LOWER":
                cap = Capitalization.LOWER;
                break;
            case "UPPER":
                cap = Capitalization.UPPER;
                break;
            case "FIRST_UPPER":
                cap = Capitalization.FIRST_UPPER;
                break;
            case "MIXED":
            default:
                cap = Capitalization.MIXED;
        }

        return generate(minLength, maxLength, cap);
    }

    public String generate(int minLength, int maxLength, Capitalization capitalization)
    {
        if(capitalization == null)
        {
            capitalization = Capitalization.MIXED;
        }

        log.fine(String.format("Generating a random String of length [%d, %d] and %s capitalization", minLength, maxLength, capitalization));

        String pattern;

        switch(capitalization)
        {
            case LOWER:
                pattern = "[a-z0-9_]";
                break;
            case UPPER:
                pattern = "[A-Z0-9_]";
                break;
            case FIRST_UPPER:
                pattern = "[A-Z0-9_][a-z0-9_]";
                break;
            case MIXED:
            default:
                pattern = "[a-zA-Z0-9_]";
                break;
        }

        pattern += String.format("{%d,%d}", minLength, maxLength);

        return new RegexGenerationMethod().generate(pattern);
    }
}
