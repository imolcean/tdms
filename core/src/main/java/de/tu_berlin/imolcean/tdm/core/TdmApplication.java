package de.tu_berlin.imolcean.tdm.core;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.dto.ProjectDto;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.core.generation.DependencyGraphCreator;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraint;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
        ProjectDto project = mapper.readValue(new ClassPathResource("EXP.tdm.json").getInputStream(), ProjectDto.class);
        projectService.open(project);

        StageContextHolder.setStageName("exp");


//        Graph<String, DefaultEdge> graph = dependencyGraphCreator.create(schemaService.getSchema(dataSourceService.getInternalDataSource()).getTables());
//
//        String start = graph.vertexSet().stream()
//                .filter(name -> name.equalsIgnoreCase("PERSON"))
//                .findAny()
//                .get();
//        Iterator<String> it = new DepthFirstIterator<>(graph, start);
//
//        System.out.println("DFT starting from: " + start);
//        while(it.hasNext())
//        {
//            System.out.println(it.next());
//        }


        System.out.println("DONE!");
    }
}
