package de.tu_berlin.imolcean.tdm.x.updaters;

import de.tu_berlin.imolcean.tdm.api.dto.SchemaUpdateCommitRequest;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.x.DiffMapper;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.report.DiffToReport;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.pf4j.Extension;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Extension
@Log
@NoArgsConstructor
public class LiquibaseUpdater implements SchemaUpdater
{
    private SchemaService schemaService;

    private String changelogPath;

    private DataSource internalDs;
    private DataSource tmpDs;

    // TODO Pack Liquibase.Core in the plugin jar
    // TODO Store intermediate DB info in plugin's config?
    // TODO Insert SchemaService
    public LiquibaseUpdater(Properties properties)
    {
        this.changelogPath = properties.getProperty("changelog.path");
        this.internalDs = null;
        this.tmpDs = null;

        log.fine("Liquibase changelog: " + this.changelogPath);
    }

    @Override
    public SchemaUpdate initSchemaUpdate(DataSource internalDs, DataSource tmpDs) throws Exception
    {
        if(changelogPath == null)
        {
            throw new IllegalStateException("Changelog path is not configured");
        }

        if(isUpdateInProgress())
        {
            throw new IllegalStateException("Another schema update is already in progress");
        }

        log.info("Initialising schema update");

        // TODO Use tmp schema instead of tmp DB?

        this.internalDs = internalDs;
        this.tmpDs = tmpDs;

        try(Connection internalDsConnection = this.internalDs.getConnection();
            Connection tmpDsConnection = this.tmpDs.getConnection())
        {
            Database internalDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(internalDsConnection));
            Database tmpDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(tmpDsConnection));

            try(Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), tmpDb))
            {
                liquibase.dropAll();
                liquibase.update(new Contexts(), new LabelExpression());

                DiffResult diff = liquibase.diff(internalDb, tmpDb, CompareControl.STANDARD);

                // TODO Remove
                new DiffToReport(diff, System.out).print();

                log.info("Update initialised");

                return new DiffMapper(schemaService, internalDs, tmpDs).toSchemaUpdate(diff);
            }
        }
    }

    @Override
    // TODO Test
    public void commitSchemaUpdate(SchemaUpdateCommitRequest request) throws Exception
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Committing schema update");

        try(Connection internalDsConnection = this.internalDs.getConnection();
            Connection tmpDsConnection = this.tmpDs.getConnection();
            Statement statement = tmpDsConnection.createStatement())
        {
            // TODO Copy data from internalDs -> tmpDs for every untouched table
            List<String> emptyTables = schemaService.getEmptyTableNames(internalDs);
            List<String> tablesToAutoMigrate = request.getAutoMigrationTables().stream()
                    .filter(table -> !emptyTables.contains(table))
                    .collect(Collectors.toList());

            log.fine("Migrating tables automatically:");

            for(String tableName : tablesToAutoMigrate)
            {
                Table internal = schemaService.getTable(internalDs, tableName);
                Table tmp = schemaService.getTable(tmpDs, tableName);

                autoMigrateData(tmpDsConnection, internal, tmp);
            }

            // TODO Perform SQL from SchemaUpdateCommitRequest for every other table

            log.fine("Migrating tables using provided migration scripts:");

            for(SchemaUpdateCommitRequest.TableDataMigrationRequest req : request.getSqlMigrationTables())
            {
                sqlMigrateData(tmpDsConnection, req.getSql());
            }

//            statement.executeUpdate("INSERT INTO DABAG_IGORDAT06_EXP.dbo.address (id, line1, line2, city, country) SELECT id, col_a, '', 'BERN', 'CH' FROM DABAG_IGORDAT06.dbo.A");

            // TODO Purge internalDs, migrate tmpDs -> internalDs
            log.fine("Committing by moving schema and data from tmpDs into internalDs");

            Database internalDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(internalDsConnection));

            try(Liquibase liquibase = new Liquibase("", new FileSystemResourceAccessor(), internalDb))
            {
//                liquibase.dropAll();
                // TODO
            }

            // TODO Purge tmpDs
            log.fine("Purging tmpDs");

            Database tmpDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(tmpDsConnection));

            try(Liquibase liquibase = new Liquibase("", new FileSystemResourceAccessor(), tmpDb))
            {
//                liquibase.dropAll();
            }
        }

        // TODO Transaction

        this.tmpDs = null;
        this.internalDs = null;

        log.info("Schema update committed");
    }

    @Override
    public void cancelSchemaUpdate() throws Exception
    {
        if(!isUpdateInProgress())
        {
            throw new IllegalStateException("There is no schema update in progress currently");
        }

        log.info("Cancelling schema update");

        try(Connection connection = tmpDs.getConnection())
        {
            Database tmpDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try(Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), tmpDb))
            {
                liquibase.dropAll();
            }
        }

        this.tmpDs = null;
        this.internalDs = null;

        log.info("Schema update cancelled");
    }

    @Override
    public boolean isUpdateInProgress()
    {
        return tmpDs != null && internalDs != null;
    }

    @Override
    public void setSchemaService(SchemaService service)
    {
        this.schemaService = service;
    }

    private void autoMigrateData(Connection connection, Table src, Table target) throws SQLException
    {
        String srcTableName = src.getFullName();
        String targetTableName = target.getFullName();
        String columnNames = src.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) SELECT %s FROM %s", targetTableName, columnNames, columnNames, srcTableName);

        log.fine(sql);

        try(Statement statement = connection.createStatement())
        {
//            statement.executeUpdate(sql);
        }
    }

    private void sqlMigrateData(Connection connection, String sql) throws SQLException
    {
        log.fine(sql);

        try(Statement statement = connection.createStatement())
        {
//            statement.executeUpdate(sql);
        }
    }
}
