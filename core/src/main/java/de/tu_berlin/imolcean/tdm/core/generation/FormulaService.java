package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.core.generation.methods.*;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.Column;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Service
@Log
public class FormulaService
{
    private final ValueLibraryService libraries;
    private final FormulaFunctionService functions;
    private final ScriptEngineManager manager;

    public FormulaService(ValueLibraryService libraries, FormulaFunctionService functions)
    {
        System.setProperty("polyglot.js.nashorn-compat", "true");

        this.libraries = libraries;
        this.functions = functions;
        this.manager = new ScriptEngineManager();

        loadGlobalEnvironment(manager);
    }

    public ScriptEngine createEngine(Column column)
    {
        ScriptEngine engine = manager.getEngineByName("graal.js");

        if(engine == null)
        {
            throw new RuntimeException("Failed to instantiate JavaScript engine");
        }

        loadEngineEnvironment(engine, column);

        return engine;
    }

    private void loadGlobalEnvironment(ScriptEngineManager manager)
    {
        for(String libName : libraries.getLibraries().keySet())
        {
            manager.put(libName, libraries.getLibraries().get(libName));
            log.info(String.format("Value library %s is loaded", libName));
        }
    }

    private void loadEngineEnvironment(ScriptEngine engine, Column column)
    {
        engine.put("Column", column);

        engine.put("RandByte", new ByteGenerationMethod());
        engine.put("RandTinyInt", new TinyIntGenerationMethod());
        engine.put("RandShort", new ShortGenerationMethod());
        engine.put("RandInt", new IntegerGenerationMethod());
        engine.put("RandLong", new LongGenerationMethod());
        engine.put("RandFloat", new FloatGenerationMethod());
        engine.put("RandDouble", new DoubleGenerationMethod());
        engine.put("RandBoolean", new BooleanGenerationMethod());
        engine.put("RandDate", new DateGenerationMethod());
        engine.put("RandTime", new TimeGenerationMethod());
        engine.put("RandTimestamp", new TimestampGenerationMethod());

        engine.put("RandString", new StringGenerationMethod(column));
        engine.put("RandDecimal", new BigDecimalGenerationMethod(column));

        engine.put("RandRegexp", new RegexGenerationMethod());
        engine.put("RandFrom", new ValueListGenerationMethod());

        // TODO Load custom generation methods

        for(String functionName : functions.getFunctions().keySet())
        {
            try
            {
                engine.eval(functions.getFunctions().get(functionName));
            }
            catch(ScriptException e)
            {
                log.warning(String.format("Function %s could not be loaded into the ScriptEngine", functionName));
                e.printStackTrace();
            }

            log.info(String.format("Function %s is loaded", functionName));
        }
    }
}
