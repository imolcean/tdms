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
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

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


//        System.setProperty("polyglot.js.nashorn-compat", "true");
//        ScriptEngine js = new ScriptEngineManager().getEngineByName("graal.js");
//
//        if(js == null)
//        {
//            System.err.println("JS failed :(");
//            System.exit(10);
//        }
//
//        js.put("Rand", new RandDateGenerationMethod());
//        js.eval("print(Rand.generate(null, null));");
//        js.eval("print(Rand.generate(null, java.sql.Date.valueOf('2021-09-01')));");
//        js.eval("print(Rand.generate(java.sql.Date.valueOf('2013-09-01'), null));");
//        js.eval("print(Rand.generate(java.sql.Date.valueOf('2013-09-01'), java.sql.Date.valueOf('2021-09-01')));");
//        js.eval("print(Rand.generate(java.sql.Date.valueOf('1834-09-01'), java.sql.Date.valueOf('3457-09-01')));");


        RandTimestampGenerationMethod rand = new RandTimestampGenerationMethod();

        System.out.println(rand.generate(null, null));
        System.out.println(rand.generate(null, Timestamp.valueOf("2021-09-01 01:23:45.78")));
        System.out.println(rand.generate(Timestamp.valueOf("2013-09-01 04:22:09"), null));
        System.out.println(rand.generate(Timestamp.valueOf("2013-09-01 00:00:00"), Timestamp.valueOf("2021-09-01 00:00:00")));


        System.out.println("DONE!");
    }
}
