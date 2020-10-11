package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import lombok.extern.java.Log;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.springframework.stereotype.Service;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log
public class DefaultDataGenerator
{
    private final DataSourceService dataSourceService;
    private final SchemaService schemaService;
    private final TableContentService tableContentService;

    public DefaultDataGenerator(DataSourceService dataSourceService,
                                SchemaService schemaService,
                                TableContentService tableContentService)
    {
        this.dataSourceService = dataSourceService;
        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    // TODO
    public void generate(Map<Table, TableRule> rules) throws SQLException, SchemaCrawlerException
    {
        //    Disable constraint checks, begin transaction
        // TODO

        //    Build dependency graph
        DefaultDirectedGraph<Table, DefaultEdge> graph = new DependencyGraphCreator().create(schemaService.getSchema(dataSourceService.getInternalDataSource()).getTables());

        // Check TableRules
        Set<Table> nodesToRemove = new HashSet<>();

        for(Table table : graph.vertexSet())
        {
            if(rules.get(table) == null || !rules.get(table).isValid())
            {
                Set<Table> successors = getAllSuccessors(graph, table);

                nodesToRemove.add(table);
                nodesToRemove.addAll(successors);

                log.warning(String.format("No valid rules are specified for table %s", table.getName()));
                log.warning(String.format(
                        "Generation will not be performed for its dependants: %s",
                        successors.stream()
                                .map(NamedObject::getName)
                                .collect(Collectors.joining(", "))));
            }
        }

        graph.removeAllVertices(nodesToRemove);

        // Detect cycles
        // For cycles, cut at one point and generate 'postponed' FKs using FkGenerationMethod of the ColumnRule, save the rule on stack
        // TODO

        // Get generation order
        List<Table> generationOrder = new ArrayList<>();
        new TopologicalOrderIterator<>(graph).forEachRemaining(generationOrder::add);

        // Generate data in order
        // TODO

        // Redo generation with FillMode::UPDATE on those Tables with 'postponed' FKs, on FK itself and all dependent Columns
        // TODO

        // Enable constraint checks, commit
        // TODO
    }

//    // TODO Delete
//    public void testSuccessors()
//    {
//        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
//
//        for(int i = 0; i <= 10; i++)
//        {
//            graph.addVertex(i);
//        }
//
//        graph.addEdge(0, 1);
//        graph.addEdge(1, 3);
//        graph.addEdge(3, 0);
//        graph.addEdge(3, 4);
//        graph.addEdge(3, 5);
//        graph.addEdge(2, 1);
//        graph.addEdge(1, 6);
//        graph.addEdge(2, 6);
//        graph.addEdge(2, 7);
//        graph.addEdge(6, 7);
//        graph.addEdge(6, 8);
//        graph.addEdge(8, 9);
//        graph.addEdge(8, 10);
//
//        print(graph);
//
//        Set<Integer> successors = getAllSuccessors(graph, 3);
//
//        System.out.println("Successors of 3:");
//        successors.forEach(System.out::println);
//
//        graph.removeAllVertices(successors);
//
//        print(graph);
//    }
//
//    // TODO Delete
//    private void print(DefaultDirectedGraph<Integer, DefaultEdge> graph)
//    {
//        System.out.println();
//
//        for(Integer i : graph.vertexSet())
//        {
//            System.out.println("Node: " + i);
//        }
//
//        for(DefaultEdge e : graph.edgeSet())
//        {
//            System.out.println("Edge: " + e.toString());
//        }
//    }

    private <V> Set<V> getAllSuccessors(DefaultDirectedGraph<V, DefaultEdge> graph, V vertex)
    {
        Set<V> successors = new HashSet<>();
        _getAllSuccessors(graph, vertex, successors);

        return successors;
    }

    private <V> void _getAllSuccessors(DefaultDirectedGraph<V, DefaultEdge> graph, V vertex, Set<V> visited)
    {
        visited.add(vertex);

        for(V successor : Graphs.successorListOf(graph, vertex))
        {
            if(!visited.contains(successor))
            {
                _getAllSuccessors(graph, successor, visited);
            }
        }
    }
}
