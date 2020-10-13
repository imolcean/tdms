package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.util.List;
import java.util.Map;

@Log
public class StringGenerationMethod implements GenerationMethod
{
    public enum Capitalization
    {
        LOWER,
        UPPER,
        FIRST_UPPER,
        MIXED,
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

    public String generate(int minLength, int maxLength, String capitalization)
    {
        return generate(minLength, maxLength, getCapitalizationFromString(capitalization));
    }

    @Override
    public Object generate(Column column, Map<String, Object> params)
    {
        List<Object> args = parseParams(params);
        int maxLength = args.get(0) != null ? ((Number) args.get(0)).intValue() : column.getType().getMaximumScale(); // TODO Check that this method return string length

        return generate(
                args.get(0) != null ? ((Number) args.get(0)).intValue() : 0,
                maxLength,
                (String) args.get(2));
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return List.of(
                new GenerationMethodParamDescription("minLength", Number.class, false),
                new GenerationMethodParamDescription("maxLength", Number.class, false),
                new GenerationMethodParamDescription("capitalization", String.class, false));
    }

    private Capitalization getCapitalizationFromString(String str)
    {
        Capitalization cap;

        if(str == null)
        {
            str = "MIXED";
        }

        switch(str.toUpperCase())
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

        return cap;
    }
}