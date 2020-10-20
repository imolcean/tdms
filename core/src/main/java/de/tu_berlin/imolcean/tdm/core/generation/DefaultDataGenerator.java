package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.api.TableContent;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import lombok.extern.java.Log;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.springframework.stereotype.Service;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
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
    // TODO FillMode::Update
    public void generate(Map<Table, TableRule> rules, Map<Table, TableContent> generated) throws SQLException, SchemaCrawlerException, IOException
    {
//        // Create temporary storage for generated data
//        Map<Table, TableContent> generated = new HashMap<>();

        // Build dependency graph
        DefaultDirectedGraph<Table, DefaultEdge> graph = new DependencyGraphCreator().create(schemaService.getSchema(dataSourceService.getInternalDataSource()).getTables());

        // Check TableRules
        checkTableRules(graph, rules);

        // Detect cycles
        List<List<Table>> cycles = DependencyGraphUtils.findCycles(graph);

        // For every cycle, cut at one point and note all 'postponed' FK Columns
        Map<Table, List<Column>> postponed = cutCycles(graph, cycles);

        // Get generation order
        List<Table> generationOrder = new ArrayList<>();
        new TopologicalOrderIterator<>(graph).forEachRemaining(generationOrder::add);

        log.fine("Generation order: " + generationOrder.stream().map(NamedObject::getName).collect(Collectors.joining(", ")));

        // Generate data in order
        log.info("Generating data (first phase)");
        for(Table table : generationOrder)
        {
            log.info("Generating data for table " + table.getName());

            TableRule tr = rules.get(table);
            TableContent rows = new TableContent(table);

            for(int i = 0; i < tr.getRowCount(); i++)
            {
                log.fine(String.format("Generating row %s/%s", i, tr.getRowCount()));

                TableContent.Row row = new TableContent.Row(table);

                for(ColumnRule cr : tr.getOrderedColumnRules())
                {
                    if(!postponed.containsKey(table) || !postponed.get(table).contains(cr.getColumn()))
                    {
                        log.fine("Generating value for column " + cr.getColumn().getName());

                        Object value = cr.getGenerationMethod().generate(cr.getParams());

                        log.fine("Value: " + value);

                        row.setValue(cr.getColumn(), value);
                    }
                }

                rows.addRow(row);
            }

            generated.put(table, rows);
        }

        // Generate 'postponed' FKs and put them in previously generated rows
        // TODO Generate Columns that are dependant from 'postponed' FKs
        log.info("Generating data (second phase)");
        for(Table table : postponed.keySet())
        {
            log.info("Generating data for postponed columns in table " + table.getName());

            TableRule tr = rules.get(table);

            for(int i = 0; i < tr.getRowCount(); i++)
            {
                log.fine(String.format("Updating row %s/%s", i, tr.getRowCount()));

                TableContent.Row row = generated.get(table).getRow(i);

                for(Column column : postponed.get(table))
                {
                    log.fine("Generating value for column " + column.getName());

                    ColumnRule cr = tr.getColumnRules().get(column);

                    Object value = cr.getGenerationMethod().generate(cr.getParams());

                    log.fine("Value: " + value);

                    row.setValue(column, value);
                }
            }
        }

        System.out.println("Generated:");
        for(Table table : generated.keySet())
        {
            System.out.println("\t" + table.getName());
            System.out.println("\t" + generated.get(table));
        }

        // Import data
        if(!generated.isEmpty())
        {
            log.fine("Writing generated data into internal DB");

            Map<Table, List<Object[]>> _generated = new HashMap<>();
            generated.forEach((table, content) -> _generated.put(table, content.getRowsAsArrays()));

            tableContentService.importData(dataSourceService.getInternalDataSource(), _generated);
        }
    }

    public void checkTableRules(DefaultDirectedGraph<Table, DefaultEdge> graph, Map<Table, TableRule> rules)
    {
        Set<Table> tablesToRemove = new HashSet<>();

        for(Table table : graph.vertexSet())
        {
            // TODO Handle non-empty tables
            // TODO Leave table in graph, if it's not empty

            if(rules.get(table) == null || !rules.get(table).isValid())
            {
                Set<Table> successors = DependencyGraphUtils.getAllSuccessors(graph, table);

                tablesToRemove.add(table);
                tablesToRemove.addAll(successors);

                log.warning(String.format("No valid rules are specified for table %s", table.getName()));
                log.warning(String.format(
                        "Generation will not be performed for its dependants: %s",
                        successors.stream()
                                .map(NamedObject::getName)
                                .collect(Collectors.joining(", "))));
            }
        }

        graph.removeAllVertices(tablesToRemove);
    }

    public Map<Table, List<Column>> cutCycles(DefaultDirectedGraph<Table, DefaultEdge> graph, List<List<Table>> cycles)
    {
        if(cycles.isEmpty())
        {
            return Collections.emptyMap();
        }

        Map<Table, Long> cyclesPerTable = cycles.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(table -> table, Collectors.counting()));

        List<Map.Entry<Table, Long>> tablesByParticipationInCycles = new ArrayList<>(cyclesPerTable.entrySet());
        tablesByParticipationInCycles.sort(Map.Entry.<Table, Long>comparingByValue().reversed());

        // TODO Remove
        System.out.println("Cycles per table (sorted):");
        tablesByParticipationInCycles.forEach(entry -> System.out.printf("%s: %d%n", entry.getKey().getName(), entry.getValue()));

        List<Table> tablesByCutPriority = tablesByParticipationInCycles.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Map<Table, List<Column>> postponed = new HashMap<>();

        for(Table table : tablesByCutPriority)
        {
            if(cycles.isEmpty())
            {
                log.fine("No cycles left");
                break;
            }

            log.fine("Cutting cycles at " + table.getName());

            List<List<Table>> cyclesOfTable = cycles.stream()
                    .filter(cycle -> cycle.contains(table))
                    .collect(Collectors.toList());

            log.fine(String.format("%s participates in %d cycles", table.getName(), cyclesOfTable.size()));
            for(List<Table> cycle : cyclesOfTable)
            {
                String str = cycle.stream()
                        .map(NamedObject::getName)
                        .collect(Collectors.joining(", "));

                log.fine(str);
            }

            if(cyclesOfTable.isEmpty())
            {
                continue;
            }

            // Determine FK Columns participating in cycles
            Set<Table> participantsOfCyclesOfTable = cyclesOfTable.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            List<Column> postponedFks = table.getColumns().stream()
                    .filter(Column::isPartOfForeignKey)
                    .filter(column -> participantsOfCyclesOfTable.contains(column.getReferencedColumn().getParent()))
                    .collect(Collectors.toList());

            log.fine(String.format("Postponed FKs in %s: %s", table.getName(), postponedFks.stream().map(NamedObject::getName).collect(Collectors.joining(", "))));

            postponed.put(table, postponedFks);

            // Remove all edges [PkTable -> table] from graph
            Set<Table> pkTables = postponedFks.stream()
                    .map(column -> column.getReferencedColumn().getParent())
                    .collect(Collectors.toSet());

            for(Table pkTable : pkTables)
            {
                graph.removeAllEdges(pkTable, table);
            }

            // Remove cyclesOfTable from cycles
            cycles.removeAll(cyclesOfTable);
        }

        return postponed;
    }
}
