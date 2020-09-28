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


//        ScriptEngine js = new ScriptEngineManager().getEngineByName("JavaScript");
//
//        if(js == null)
//        {
//            System.err.println("JS failed :(");
//            System.exit(10);
//        }
//
//        js.put("Rand", new RegexGenerationMethod());
//        js.eval("print(Rand.generate('(a|b|c){3,5}'));");
//        js.eval("print(Rand.generate('\\\\w'));");


//        RegexGenerationMethod rand = new RegexGenerationMethod();
//
//        System.out.println(rand.generate("(a|b|c){3,5}"));
//        System.out.println(rand.generate("\\w"));


        System.out.println("DONE!");
    }
}
