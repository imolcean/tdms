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
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

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
    private ValueLibraryService valueLibraryService;

    @Autowired
    private DefaultDataGenerator defaultDataGenerator;

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

//        Column column = schemaService.getTable(dataSourceService.getInternalDataSource(), "person").getColumns().get(1);
//        System.out.println(column.getFullName());
//
//        GenerationMethod rand = GenerationMethods.createByColumn(column);
//        System.out.println(rand.getClass());
//
//        Map<String, Object> params = new HashMap<>();
//
//        params.put("minLength", 3);
//        params.put("maxLength", 6);
//        params.put("capitalization", "mixed");
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("minLength", 3);
//        params.put("maxLength", 6);
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("minLength", 3);
//        params.put("capitalization", "lower");
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("maxLength", 6);
//        params.put("capitalization", "upper");
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("capitalization", "first_upper");
//        System.out.println(rand.generate(params));


//        defaultDataGenerator.generate();
//        defaultDataGenerator.testSuccessors();

        DefaultDirectedGraph<Table, DefaultEdge> graph = new DependencyGraphCreator().create(schemaService.getSchema(dataSourceService.getInternalDataSource()).getTables());
//        defaultDataGenerator.visualize(graph, "test");
        defaultDataGenerator.findCycles(graph);


        System.out.println("DONE!");
    }
}
