package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.TableNotFoundException;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.DataService;
import de.tu_berlin.imolcean.tdm.core.StreamResourceAccessor;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log
public class DefaultSchemaService implements SchemaService
{
    // TODO Remove dependency on DataService

    private final DataService dataService;

    private Catalog cache;

    public DefaultSchemaService(DataService dataService)
    {
        this.dataService = dataService;
        this.cache = null;
    }

    @Override
    public Catalog getSchema(DataSource ds) throws SQLException, SchemaCrawlerException
    {
        if(cache != null)
        {
            log.fine("Cached schema found");
            return cache;
        }

        log.fine("Retrieving schema");

        try(Connection connection = ds.getConnection())
        {
            LoadOptions load = LoadOptionsBuilder.builder()
                    .withInfoLevel(InfoLevel.standard)
                    .toOptions();

            SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                    .withLimitOptions(getDefaultLimitOptions(getFullSchemaName(connection)))
                    .withLoadOptions(load)
                    .toOptions();

            cache = SchemaCrawlerUtility.getCatalog(connection, options);

            return cache;
        }
    }

    @Override
    public void copySchema(DataSource src, DataSource target) throws Exception
    {
        cache = null;

        Connection srcConnection = src.getConnection();
        Connection targetConnection = target.getConnection();

        log.info(String.format("Copying schema from %s to %s", srcConnection.getCatalog(), targetConnection.getCatalog()));

        Database tmpDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(srcConnection));
        Database internalDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));

        try(Liquibase liqSrc = new Liquibase("", new FileSystemResourceAccessor(), tmpDb);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8))
        {
            DiffToChangeLog writer =
                    new DiffToChangeLog(
                            new DiffOutputControl(
                                    true,
                                    true,
                                    true,
                                    new CompareControl.SchemaComparison[0]));

            liqSrc.generateChangeLog(tmpDb.getDefaultSchema(), writer, ps, new YamlChangeLogSerializer());

            // Change catalogName from src to tmp
            // TODO Use Liquibase API to change fields in Change. Replacing with RegEx may have side effects.
            String changelog = baos.toString().replaceAll(srcConnection.getCatalog(), targetConnection.getCatalog());

            // TODO Remove
            System.out.println(changelog);

            try(Liquibase liqTarget = new Liquibase("changelog.yml", new StreamResourceAccessor(changelog), internalDb))
            {
                liqTarget.update(new Contexts());

                // We use Liquibase as a service tool to copy schema from one DataSource to another,
                // which means that we get non-empty tables DATABASECHANGELOG nad DATABASECHANGELOGLOCK
                // as a side effect. After the schema is copied, we need to determine whether the source DB
                // used to have these tables. If so, we will need to keep them in the target DB as well.
                // The tables, however, will need to be cleared so they do not contain any rows.
                // If the source DB did not have these tables, which means the source DB was not
                // managed with Liquibase, we will need to remove the tables from the target DB
                // too so the two schemas are identical.

                if(tableExists(src, "DATABASECHANGELOG"))
                {
                    dataService.clearTable(target, getTable(target, "DATABASECHANGELOG"));
                    dataService.clearTable(target, getTable(target, "DATABASECHANGELOGLOCK"));
                }
                else
                {
                    dropTable(target, "DATABASECHANGELOG");
                    dropTable(target, "DATABASECHANGELOGLOCK");
                }
            }
        }

        log.info("Schema copied");
    }

    @Override
    public void purgeSchema(DataSource ds) throws Exception
    {
        cache = null;

        log.info("Purging schema");

        Database db = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(ds.getConnection()));

        try(Liquibase liquibase = new Liquibase("", new FileSystemResourceAccessor(), db))
        {
            liquibase.dropAll();
        }

        log.info("Schema purged");
    }

    @Override
    public List<String> getTableNames(DataSource ds) throws SQLException, SchemaCrawlerException
    {
        try(Connection connection = ds.getConnection())
        {
            LoadOptions load = LoadOptionsBuilder.builder()
                    .withInfoLevel(InfoLevel.minimum)
                    .toOptions();

            SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                    .withLimitOptions(getDefaultLimitOptions(getFullSchemaName(connection)))
                    .withLoadOptions(load)
                    .toOptions();

            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

            return catalog.getTables().stream()
                    .map(NamedObject::getName)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getOccupiedTableNames(DataSource ds) throws SQLException, SchemaCrawlerException
    {
        try(Connection connection = ds.getConnection())
        {
            LoadOptions load = LoadOptionsBuilder.builder()
                    .withInfoLevel(InfoLevel.minimum)
                    .loadRowCounts()
                    .toOptions();

            FilterOptions filter = FilterOptionsBuilder.builder()
                    .noEmptyTables()
                    .toOptions();

            SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                    .withLimitOptions(getDefaultLimitOptions(getFullSchemaName(connection)))
                    .withLoadOptions(load)
                    .withFilterOptions(filter)
                    .toOptions();

            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

            return catalog.getTables().stream()
                    .map(NamedObject::getName)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getEmptyTableNames(DataSource ds) throws SQLException, SchemaCrawlerException
    {
        List<String> allTables = getTableNames(ds);
        List<String> occupiedTables = getOccupiedTableNames(ds);

        return allTables.stream()
                .filter(table -> !occupiedTables.contains(table))
                .collect(Collectors.toList());
    }

    @Override
    public Table getTable(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException
    {
        if(cache != null)
        {
            return cache.getTables().stream()
                    .filter(table -> table.getName().equals(tableName))
                    .findFirst()
                    .orElseThrow(() -> new TableNotFoundException(tableName));
        }

        try(Connection connection = ds.getConnection())
        {
            LimitOptions limit = LimitOptionsBuilder.builder()
                    .fromOptions(getDefaultLimitOptions(getFullSchemaName(connection)))
                    .includeTables(new RegularExpressionInclusionRule(getFullTableName(connection, tableName)))
                    .toOptions();

            LoadOptions load = LoadOptionsBuilder.builder()
                    .withInfoLevel(InfoLevel.standard)
                    .toOptions();

            SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                    .withLimitOptions(limit)
                    .withLoadOptions(load)
                    .toOptions();

            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

            return catalog.getTables().stream()
                    .findFirst()
                    .orElseThrow(() -> new TableNotFoundException(tableName));
        }
    }

    @Override
    public List<Table> getTables(DataSource ds, Collection<String> tableNames) throws SQLException, SchemaCrawlerException
    {
        List<Table> tables = new ArrayList<>(tableNames.size());

        for(String tableName : tableNames)
        {
            tables.add(getTable(ds, tableName));
        }

        return tables;
    }

    @Override
    public Optional<Column> findColumn(Table table, String columnName)
    {
        return Optional.ofNullable(
                table.getColumns().stream()
                        .filter(column -> column.getName().equals(columnName))
                        .collect(Collectors.toList())
                        .get(0));
    }

    @Override
    public boolean tableExists(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException
    {
        try
        {
            getTable(ds, tableName);

            return true;
        }
        catch(TableNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public void dropTable(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException
    {
        if(!tableExists(ds, tableName))
        {
            throw new TableNotFoundException(tableName);
        }

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement())
        {
            statement.executeUpdate("DROP TABLE " + tableName);
        }
    }

    private String getFullSchemaName(Connection connection) throws SQLException
    {
        return String.format("%s.%s", connection.getCatalog(), connection.getSchema());
    }

    private String getFullTableName(Connection connection, String tableName) throws SQLException
    {
        return String.format("%s.%s", getFullSchemaName(connection), tableName);
    }

    private LimitOptions getDefaultLimitOptions(String fullSchemaName)
    {
        return LimitOptionsBuilder.builder()
                .includeSchemas(new RegularExpressionInclusionRule(fullSchemaName))
                .includeTables(name -> !name.contains("sysdiagrams"))
                .tableTypes("TABLE")
                .toOptions();
    }
}
