package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.TableContent;
import de.tu_berlin.imolcean.tdm.api.dto.TableRuleDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator.DataGenerator;
import de.tu_berlin.imolcean.tdm.api.services.LowLevelDataService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.DataService;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log
public class RuleBasedDataGenerator implements DataGenerator
{
    private final SchemaService schemaService;
    private final DataService dataService;
    private final LowLevelDataService lowLevelDataService;
    private final GenerationMethodCreator generationMethodCreator;

    public RuleBasedDataGenerator(SchemaService schemaService,
                                  DataService dataService,
                                  LowLevelDataService lowLevelDataService,
                                  GenerationMethodCreator generationMethodCreator)
    {
        this.schemaService = schemaService;
        this.dataService = dataService;
        this.lowLevelDataService = lowLevelDataService;
        this.generationMethodCreator = generationMethodCreator;
    }

    @Override
    public void generate(DataSourceWrapper ds)
    {
        throw new UnsupportedOperationException("This data generator requires table rules to perform generation");
    }

    // TODO Test!

    public void generate(DataSourceWrapper ds, Collection<TableRuleDto> dtos) throws SchemaCrawlerException, IOException, SQLException
    {
        // Disable constraints
        lowLevelDataService.disableConstraints(ds);

        // Start transaction
        try(Connection connection = lowLevelDataService.createTransaction(ds))
        {
            // Create rules from DTOs
            Map<Table, TableRule> rules = getRulesFromDtos(dtos, ds, connection);

            // Build dependency graph
            DefaultDirectedGraph<Table, DefaultEdge> graph = new DependencyGraphCreator().createForTables(schemaService.getSchema(ds).getTables());

            // Validate table rules. Remove invalid ones and their dependants from the graph.
            checkTableRules(graph, rules, ds);

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

                    log.fine(String.format("Looking for dependants of column %s", column.getName()));

                    for(ColumnRule _cr : rules.get(table).getColumnRules().values())
                    {
                        if(_cr.getDependencies().contains(column))
                        {
                            log.fine(String.format("Found column %s, marking it as postponed", _cr.getColumn().getName()));

                            _cr.setPostponed(true);
                        }
                    }
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

            try
            {
                // Generate data in order
                log.info("Generating data (first phase)");
                for(TableRule tr : generationOrder)
                {
                    List<TableContent.Row> rows = lowLevelDataService.getTableContent(connection, tr.getTable()).stream()
                            .map(rawObjects -> new TableContent.Row(tr.getTable(), rawObjects))
                            .collect(Collectors.toList());
                    TableContent content = new TableContent(tr.getTable(), rows);

                    tr.generate(content);

                    lowLevelDataService.clearTable(connection, tr.getTable());
                    lowLevelDataService.insertRows(connection, tr.getTable(), content.getRowsAsArrays());
                }

                // Generate 'postponed' FKs and put them in previously generated rows
                log.info("Generating data (second phase)");
                List<TableRule> postponed = rules.values().stream()
                        .filter(TableRule::isPostponed)
                        .collect(Collectors.toList());

                for(TableRule tr : postponed)
                {
                    List<ColumnRule> postponedColumnRules = tr.getOrderedPostponedColumnRules();
                    postponedColumnRules.forEach(cr -> cr.setPostponed(false));

                    tr.setFillMode(TableRule.FillMode.UPDATE);
                    tr.setColumnRules(postponedColumnRules);

                    List<TableContent.Row> rows = lowLevelDataService.getTableContent(connection, tr.getTable()).stream()
                            .map(rawObjects -> new TableContent.Row(tr.getTable(), rawObjects))
                            .collect(Collectors.toList());
                    TableContent content = new TableContent(tr.getTable(), rows);

                    tr.generate(content);

                    lowLevelDataService.clearTable(connection, tr.getTable());
                    lowLevelDataService.insertRows(connection, tr.getTable(), content.getRowsAsArrays());
                }
            }
            catch(Exception e)
            {
                lowLevelDataService.rollbackTransaction(connection);
                log.warning("Something went wrong during generation. All changes are rolled back.");
                throw e;
            }

            // Commit transaction
            lowLevelDataService.commitTransaction(connection);
        }

        // Enable constraints
        lowLevelDataService.enableConstraints(ds);
    }

    private Map<Table, TableRule> getRulesFromDtos(Collection<TableRuleDto> dtos, DataSourceWrapper ds, Connection connection) throws SQLException, SchemaCrawlerException
    {
        Map<Table, TableRule> rules = new HashMap<>();

        for(TableRuleDto trDto : dtos)
        {
            Table table = schemaService.getTable(ds, trDto.getTableName());
            TableRule tr = new TableRule(table, TableRule.FillMode.valueOf(trDto.getFillMode().name()), trDto.getRowCount());

            for(TableRuleDto.ColumnRuleDto crDto : trDto.getColumnRules())
            {
                ColumnRule cr =
                        new ColumnRule(
                                tr,
                                table.getColumns().stream()
                                        .filter(column -> column.getName().equals(crDto.getColumnName()))
                                        .collect(Collectors.toList())
                                        .get(0),
                                generationMethodCreator.create(crDto, table, connection),
                                crDto.isUniqueValues(),
                                crDto.getNullPart(),
                                crDto.getParams());

                tr.putColumnRule(cr);
            }

            rules.put(tr.getTable(), tr);
        }

        return rules;
    }

    private void checkTableRules(DefaultDirectedGraph<Table, DefaultEdge> graph, Map<Table, TableRule> rules, DataSourceWrapper ds) throws SQLException
    {
        Set<Table> tablesToRemove = new HashSet<>();

        for(Table table : graph.vertexSet())
        {
            if(!dataService.isTableEmpty(ds, table))
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

    private Map<Table, List<Column>> cutCycles(DefaultDirectedGraph<Table, DefaultEdge> graph, List<List<Table>> cycles)
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
