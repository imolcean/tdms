package de.tu_berlin.imolcean.tdm.core;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.dto.ProjectDto;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.core.generation.*;
import de.tu_berlin.imolcean.tdm.core.generation.methods.*;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    private ProjectService projectService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DependencyGraphCreator dependencyGraphCreator;

    private final ObjectMapper mapper = new ObjectMapper()
            .setDefaultPrettyPrinter(
                    new DefaultPrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        ProjectDto project = mapper.readValue(new ClassPathResource("RU2.tdm.json").getInputStream(), ProjectDto.class);
        projectService.open(project);

        StageContextHolder.setStageName("exp");


        ScriptEngine js = new ScriptEngineManager().getEngineByName("JavaScript");

        if(js == null)
        {
            System.err.println("JS failed :(");
            System.exit(10);
        }

        js.put("Rand", new RandBooleanGenerationMethod());
//        js.eval("print(Rand.generate(null, null));");
//        js.eval("print(Rand.generate(null, 3.3));");
//        js.eval("print(Rand.generate(3e1, null));");
//        js.eval("print(Rand.generate(129, -130));");
        js.eval("print(Rand.generate());");


//        RandLongGenerationMethod rand = new RandLongGenerationMethod();
//
//        System.out.println(rand.generate(null, null));
//        System.out.println(rand.generate(null, 100.25));
//        System.out.println(rand.generate(-43.33, null));
//        System.out.println(rand.generate(-43, 100));
//        System.out.println(rand.generate(-129000000000L, 129000000000L));


        System.out.println("DONE!");
    }
}
