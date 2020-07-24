package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.TableNotFoundException;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import org.springframework.stereotype.Service;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultSchemaService implements SchemaService
{
    // TODO Cache

    @Override
    public Catalog getSchema(DataSource ds) throws SQLException, SchemaCrawlerException
    {
        try(Connection connection = ds.getConnection())
        {
            LoadOptions load = LoadOptionsBuilder.builder()
                    .withInfoLevel(InfoLevel.standard)
                    .toOptions();

            SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                    .withLimitOptions(getDefaultLimitOptions(getFullSchemaName(connection)))
                    .withLoadOptions(load)
                    .toOptions();

            return SchemaCrawlerUtility.getCatalog(connection, options);
        }
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
