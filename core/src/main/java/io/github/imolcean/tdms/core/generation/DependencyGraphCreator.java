package io.github.imolcean.tdms.core.generation;

import lombok.extern.java.Log;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.Table;

import java.util.Collection;
import java.util.List;

@Log
public class DependencyGraphCreator
{
    public DefaultDirectedGraph<Table, DefaultEdge> createForTables(Collection<Table> schema)
    {
        log.info("Creating dependency graph for Tables");

        DefaultDirectedGraph<Table, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Create a Node for each Table
        for(Table table : schema)
        {
            graph.addVertex(table);

            log.fine(String.format("Node '%s' added to the dependency graph", table.getName()));
        }

        // Create an Edge [PK Table -> FK Table] for every FK
        for(Table table : schema)
        {
            for(ForeignKey fk : table.getImportedForeignKeys())
            {
                List<ForeignKeyColumnReference> refs = fk.getColumnReferences();
                if(refs.size() > 0)
                {
                    Table referencedTable = refs.get(0).getPrimaryKeyColumn().getParent();

                    graph.addEdge(referencedTable, table);

                    log.fine(String.format("Edge '%s -> %s' added to the dependency graph", referencedTable.getName(), table.getName()));
                }
            }
        }

        return graph;
    }

    public DefaultDirectedGraph<Column, DefaultEdge> createForColumns(TableRule tableRule)
    {
        log.info("Creating dependency graph for Columns");

        DefaultDirectedGraph<Column, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Create a Node for each Column
        for(Column column : tableRule.getTable().getColumns())
        {
            graph.addVertex(column);

            log.fine(String.format("Node '%s' added to the dependency graph", column.getName()));
        }

        // Create an Edge [Dependency Column -> Dependant Column] for every dependency
        for(ColumnRule rule : tableRule.getColumnRules().values())
        {
            for(Column dependency : rule.getDependencies())
            {
                graph.addEdge(dependency, rule.getColumn());

                log.fine(String.format("Edge '%s -> %s' added to he dependency graph", dependency.getName(), rule.getColumn().getName()));
            }
        }

        return graph;
    }
}
