package de.tu_berlin.imolcean.tdm.core.generation;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.TiernanSimpleCycles;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log
public class DependencyGraphUtils
{
    @SneakyThrows
    public static void visualize(Graph<Table, DefaultEdge> g, String name)
    {
        File imgFile = new File(String.format("C:\\tdm\\graphs\\%s.png", name));
        while(!imgFile.createNewFile())
        {
            name = String.format("%s_copy", name);
            imgFile = new File(String.format("C:\\tdm\\graphs\\%s.png", name));
        }

        JGraphXAdapter<Table, DefaultEdge> adapter = new JGraphXAdapter<Table, DefaultEdge>(g);
        mxIGraphLayout layout = new mxCircleLayout(adapter);
        layout.execute(adapter.getDefaultParent());
        BufferedImage image = mxCellRenderer.createBufferedImage(adapter, null, 2, Color.WHITE, true, null);

        ImageIO.write(image, "PNG", imgFile);
    }

    public static List<List<Table>> findCycles(DefaultDirectedGraph<Table, DefaultEdge> graph)
    {
        log.info("Looking for cycles in dependency graph");

        List<List<Table>> cycles = new TiernanSimpleCycles<>(graph).findSimpleCycles();

        log.fine(String.format("Dependency Graph contains %s cycles", cycles.size()));
        for(List<Table> cycle : cycles)
        {
            String str = cycle.stream()
                    .map(NamedObject::getName)
                    .collect(Collectors.joining(", "));

            log.fine(str);
        }

        return cycles;
    }

    public static <V> Set<V> getAllSuccessors(DefaultDirectedGraph<V, DefaultEdge> graph, V vertex)
    {
        Set<V> successors = new HashSet<>();
        _getAllSuccessors(graph, vertex, successors);

        return successors;
    }

    static private <V> void _getAllSuccessors(DefaultDirectedGraph<V, DefaultEdge> graph, V vertex, Set<V> visited)
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
