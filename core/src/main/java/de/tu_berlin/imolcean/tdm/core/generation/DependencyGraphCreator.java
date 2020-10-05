package de.tu_berlin.imolcean.tdm.core.generation;

import lombok.extern.java.Log;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Service;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.Table;

import java.util.Collection;
import java.util.List;

@Service
@Log
public class DependencyGraphCreator
{
    public Graph<String, DefaultEdge> create(Collection<Table> schema)
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Create a Node for each Table
        for(Table table : schema)
        {
            graph.addVertex(table.getName());

            log.fine(String.format("Node '%s' added to the dependency graph", table.getName()));
        }

        // Create an Edge [FK Table -> PK Table] for every FK
        for(Table table : schema)
        {
            for(ForeignKey fk : table.getImportedForeignKeys())
            {
                List<ForeignKeyColumnReference> refs = fk.getColumnReferences();
                if(refs.size() > 0)
                {
                    Table referencedTable = refs.get(0).getPrimaryKeyColumn().getParent();

                    graph.addEdge(table.getName(), referencedTable.getName());

                    log.fine(String.format("Edge '%s -> %s' added to the dependency graph", table.getName(), referencedTable.getName()));
                }
            }
        }

        return graph;
    }
}