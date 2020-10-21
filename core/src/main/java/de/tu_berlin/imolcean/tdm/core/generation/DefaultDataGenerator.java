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
    public void generate(Map<Table, TableRule> rules, Map<Table, TableContent> data) throws SQLException, SchemaCrawlerException, IOException
    {
//        // Create temporary storage for generated data
//        Map<Table, TableContent> data = new HashMap<>();

        // Build dependency graph
        DefaultDirectedGraph<Table, DefaultEdge> graph = new DependencyGraphCreator().create(schemaService.getSchema(dataSourceService.getInternalDataSource()).getTables());

        // Check TableRules
        checkTableRules(graph, rules);

        // Detect cycles
        List<List<Table>> cycles = DependencyGraphUtils.findCycles(graph);

        // For every cycle, cut at one point and mark all 'postponed' FK Columns
        Map<Table, List<Column>> table2postponedColumns = cutCycles(graph, cycles);
        for(Table table : table2postponedColumns.keySet())
        {
            log.fine(String.format("Marking table %s as postponed", table.getName()));

            TableRule tr = rules.get(table);
            if(tr == null)
            {
                log.fine("No TableRule found, skipping the table");
                continue;
            }

            for(Column column : table2postponedColumns.get(table))
            {
                log.fine(String.format("Marking column %s as postponed", column.getName()));

                ColumnRule cr = rules.get(table).getColumnRules().get(column);
                if(cr == null)
                {
                    log.fine("No ColumnRule found, skipping the column");
                    continue;
                }

                cr.setPostponed(true);
            }
        }

        // Get generation order
        List<TableRule> generationOrder = new ArrayList<>();
        new TopologicalOrderIterator<>(graph).forEachRemaining(table -> {
            if(rules.get(table) != null)
            {
                generationOrder.add(rules.get(table));
            }
        });

        log.fine("Generation order: " + generationOrder.stream().map(tr -> tr.getTable().getName()).collect(Collectors.joining(", ")));

        // Generate data in order
        log.info("Generating data (first phase)");
        for(TableRule tr : generationOrder)
        {
            List<TableContent.Row> rows = tableContentService.getTableContent(dataSourceService.getInternalDataSource(), tr.getTable()).stream()
                    .map(rawObjects -> new TableContent.Row(tr.getTable(), rawObjects))
                    .collect(Collectors.toList());
            TableContent content = new TableContent(tr.getTable(), rows);

            tr.generate(content);
            data.put(tr.getTable(), content);
        }

        // Generate 'postponed' FKs and put them in previously generated rows
        log.info("Generating data (second phase)");
        // TODO Generate Columns that are dependant from 'postponed' FKs
        List<TableRule> postponed = rules.values().stream()
                .filter(TableRule::isPostponed)
                .collect(Collectors.toList());

        for(TableRule tr : postponed)
        {
            List<ColumnRule> postponedColumnRules = tr.getPostponedColumnRules();
            postponedColumnRules.forEach(cr -> cr.setPostponed(false));

            tr.setFillMode(TableRule.FillMode.UPDATE);
            tr.setColumnRules(postponedColumnRules);

            tr.generate(data.get(tr.getTable()));
        }

        System.out.println("Generated:");
        for(Table table : data.keySet())
        {
            System.out.println("\t" + table.getName());
            System.out.println("\t" + data.get(table));
        }

        // Import data
        // TODO Use low level calls: disable/enable constraints, control over transaction
        if(!data.isEmpty())
        {
            log.fine("Writing generated data into internal DB");

            Map<Table, List<Object[]>> _data = new HashMap<>();
            data.forEach((table, content) -> _data.put(table, content.getRowsAsArrays()));

            for(Table table : data.keySet())
            {
                tableContentService.clearTable(dataSourceService.getInternalDataSource(), table);
            }

            tableContentService.importData(dataSourceService.getInternalDataSource(), _data);
        }
    }

    public void checkTableRules(DefaultDirectedGraph<Table, DefaultEdge> graph, Map<Table, TableRule> rules) throws SQLException
    {
        Set<Table> tablesToRemove = new HashSet<>();

        for(Table table : graph.vertexSet())
        {
            if(!tableContentService.isTableEmpty(dataSourceService.getInternalDataSource(), table))
            {
                continue;
            }

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
